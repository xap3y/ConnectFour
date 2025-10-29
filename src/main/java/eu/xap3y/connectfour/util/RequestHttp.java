package eu.xap3y.connectfour.util;

import eu.xap3y.connectfour.ConnectFour;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class RequestHttp {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private RequestHttp() {}

    public static CompletableFuture<UpdateCheckResult> isNewest() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ConnectFour.VERSION_UPSTREAM_URL))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((resp, ex) -> {
                    if (ex != null || resp == null || resp.statusCode() / 100 != 2) {
                        return new UpdateCheckResult(false, null);
                    }
                    String body = resp.body() == null ? "" : resp.body().trim();
                    String verNewestDigits = body.replace(".", "");
                    String currentDigits = ConnectFour.VERSION.replace(".", "");
                    int newest = toIntSafe(verNewestDigits);
                    int current = toIntSafe(currentDigits);
                    if (current < newest) {
                        return new UpdateCheckResult(false, body);
                    }
                    return new UpdateCheckResult(true, null);
                });
    }

    private static int toIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    public record UpdateCheckResult(boolean isUpToDate, String latestVersion) {
    }
}