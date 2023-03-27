package Model;

import io.swagger.client.model.SwipeDetails;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class SwipeActionGenerator implements Runnable {
    private static final int swiperStartId = 1;
    private static final int swiperEndId = 5000;
    private static final int swipeeStartId = 1;
    private static final int swipeeEndId = 1000000;
    private static final int commentLength = 256;
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    private BlockingQueue<SwipeAction> swipeActions;
    private int total_num;

    public SwipeActionGenerator(BlockingQueue<SwipeAction> swipeActions, int total_num) {
        this.swipeActions = swipeActions;
        this.total_num = total_num;
    }

    @Override
    public void run() {
        for (int i = 0; i < this.total_num; i++){
            Random random = new Random();
            Integer swiperId = ThreadLocalRandom.current().nextInt(swiperStartId,swiperEndId+1);
            Integer swipeeId = ThreadLocalRandom.current().nextInt(swipeeStartId,swipeeEndId+1);
            Boolean swipeLeft = ThreadLocalRandom.current().nextBoolean();
            String comment = random.ints(leftLimit, rightLimit + 1)
                    .limit(commentLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            SwipeDetails swipeDetail = new SwipeDetails();
            swipeDetail.setSwiper(swiperId.toString());
            swipeDetail.setSwipee(swipeeId.toString());
            swipeDetail.setComment(comment);
            SwipeAction swipeAction = new SwipeAction(swipeLeft,swipeDetail);
            this.swipeActions.offer(swipeAction); //adding to the queue
        }

    }
}
