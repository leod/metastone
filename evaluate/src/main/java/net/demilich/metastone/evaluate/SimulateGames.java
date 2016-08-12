package net.demilich.metastone.evaluate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;

public class SimulateGames {
    private class PlayGameTask implements Callable<Void> {
        private final GameConfig gameConfig;

        public PlayGameTask(GameConfig gameConfig) {
            this.gameConfig = gameConfig;
        }

        @Override
        public Void call() throws Exception {
            PlayerConfig playerConfig1 = gameConfig.getPlayerConfig1();
            PlayerConfig playerConfig2 = gameConfig.getPlayerConfig2();

            Player player1 = new Player(playerConfig1);
            Player player2 = new Player(playerConfig2);

            DeckFormat deckFormat = gameConfig.getDeckFormat();
            GameContext newGame = new GameContext(player1, player2, new GameLogic(), deckFormat);
            newGame.play();

            onGameComplete(gameConfig, newGame);
            newGame.dispose();

            return null;
        }

    }

    private int p1wins;
    private int gamesCompleted;

    public int execute(final GameConfig gameConfig) {
        Thread t = new Thread(() -> {
            int cores = 4;
            System.err.println("Starting simulation on " + cores + " cores");
            ExecutorService executor = Executors.newFixedThreadPool(cores);

            List<Future<Void>> futures = new ArrayList<>();

            // queue up all games as tasks
            for (int i = 0; i < gameConfig.getNumberOfGames(); i++) {
                PlayGameTask task = new PlayGameTask(gameConfig);
                Future<Void> future = executor.submit(task);
                futures.add(future);
            }

            executor.shutdown();
            boolean completed = false;
            while (!completed) {
                completed = true;
                for (Future<Void> future : futures) {
                    if (!future.isDone()) {
                        completed = false;
                        continue;
                    }
                    try {
                        future.get();
                    } catch (Exception e) {
                        System.err.println(e);
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
                futures.removeIf(future -> future.isDone());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.err.println("Simulation completed");
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            System.err.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
        assert gamesCompleted == gameConfig.getNumberOfGames();
        return p1wins;
    }

    private void onGameComplete(GameConfig gameConfig, GameContext context) {

        gamesCompleted++;
        assert context.getWinningPlayerId() == 0 || context.getWinningPlayerId() == 1;
        if (context.getWinningPlayerId() == 0) {
            p1wins++;
            System.out.print('+');
        } else {
            System.out.print('-');
        }

        System.err.flush();
    }

}

