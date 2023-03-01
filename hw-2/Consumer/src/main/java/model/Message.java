package model;

import org.jetbrains.annotations.NotNull;

public class Message {
    @NotNull private String action;

    @NotNull private String swiper;

    @NotNull private String swipee;

    private String comment;

    public Message(String action, String swiper, String swipee, String comment) {
        this.action = action;
        this.swiper = swiper;
        this.swipee = swipee;
        this.comment = comment;
    }

    public Message() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public String getSwipee() {
        return swipee;
    }

    public void setSwipee(String swipee) {
        this.swipee = swipee;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
