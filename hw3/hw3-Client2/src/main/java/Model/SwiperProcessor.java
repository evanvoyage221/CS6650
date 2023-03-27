package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SwiperProcessor implements Runnable{
    private String urlBase;
    private AtomicInteger successReq;
    private AtomicInteger failReq;
    private int totalReq;
    private BlockingQueue<SwipeAction> actions;
    private CountDownLatch latch;
    private List<String[]> records;

    public SwiperProcessor(String urlBase,AtomicInteger successReq, AtomicInteger failReq, int totalReq, BlockingQueue<SwipeAction> actions, CountDownLatch latch,List<String[]> records) {
        this.urlBase = urlBase;
        this.successReq = successReq;
        this.failReq = failReq;
        this.totalReq = totalReq;
        this.actions = actions;
        this.latch = latch;
        this.records = records;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        SwipeApi swipeApi = new SwipeApi(apiClient);
        swipeApi.getApiClient().setBasePath(this.urlBase);
        int successCount = 0;
        int failCount = 0;

        for(int i = 0; i < this.totalReq; i++){
            SwipeAction currSwipeAction = null;
            try {
                currSwipeAction = this.actions.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(postRequest(swipeApi,currSwipeAction)){
                successCount++;
            }else{
                failCount++;
            }
        }

        successReq.getAndAdd(successCount);
        failReq.getAndAdd(failCount);
        latch.countDown();
    }

    public boolean postRequest(SwipeApi swipeApi, SwipeAction swipeAction){
        int retryTime = 0;
        while (retryTime < 5){
            try {
                long start = System.currentTimeMillis();

                ApiResponse<Void> res = swipeApi.swipeWithHttpInfo(swipeAction.getBody(), swipeAction.getLeftOrRight() ? "left":"right");
                if (res.getStatusCode() == 201) {
                    long end = System.currentTimeMillis();
                    System.out.println(end - start);
                    String[] curRecord = new String[4];
                    curRecord[0] = String.valueOf(start);
                    curRecord[1] = "POST";
                    curRecord[2] = String.valueOf(end - start);
                    curRecord[3] = String.valueOf(res.getStatusCode());
                    this.records.add(curRecord);
                    return true;
                }
            } catch (ApiException e) {
                retryTime++;
                e.printStackTrace();
            }
        }
        return false;
    }
}
