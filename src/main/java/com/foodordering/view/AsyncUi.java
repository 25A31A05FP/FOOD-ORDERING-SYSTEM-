package com.foodordering.view;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

public final class AsyncUi {
    private AsyncUi() {
    }

    public static <T> void run(JComponent parent, Callable<T> operation, Consumer<T> success) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operation.call();
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    Theme.showError(parent, exception);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    Theme.showError(parent, cause instanceof Exception appException
                            ? appException : new Exception("The request failed.", cause));
                }
            }
        }.execute();
    }
}
