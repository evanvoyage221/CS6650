package Model;

import io.swagger.client.model.SwipeDetails;

public class SwipeAction {
    private Boolean leftOrRight;
    private SwipeDetails body;

    public SwipeAction(Boolean leftOrRight, SwipeDetails body) {
        this.leftOrRight = leftOrRight;
        this.body = body;
    }

    public SwipeAction() {}

    public Boolean getLeftOrRight() {
        return leftOrRight;
    }

    public SwipeDetails getBody() {
        return body;
    }

    public void setLeftOrRight(Boolean leftOrRight) {
        this.leftOrRight = leftOrRight;
    }

    public void setBody(SwipeDetails body) {
        this.body = body;
    }
}
