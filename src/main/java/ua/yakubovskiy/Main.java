package ua.yakubovskiy;

public class Main {
    public static void main(String[] args) {
        ProcessingRequest processingRequest =
                new ProcessingRequest("https://api.binance.com/api/v3/depth?limit=5000&symbol=LINKUSDT",
                        "GET");

        while (true) {
            processingRequest.start();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}