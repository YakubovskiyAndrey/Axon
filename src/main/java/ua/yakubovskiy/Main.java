package ua.yakubovskiy;

public class Main {
    public static void main(String[] args) {
        ProcessingRequest processingRequest =
                new ProcessingRequest("https://api.binance.com/api/v3/depth?limit=5000&symbol=LINKUSDT",
                        "GET");
        processingRequest.start();
    }
}