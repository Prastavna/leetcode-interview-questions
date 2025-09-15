package com.prastavna.leetcode.models;

import java.util.List;

public class DiscussPostDetail {
    private UgcArticleDiscussionArticle ugcArticleDiscussionArticle;

    public static class UgcArticleDiscussionArticle {
        private String uuid;
        private String title;
        private String slug;
        private String summary;
        private String content;
        private boolean isSlate;

        private Author author;
        private boolean isOwner;
        private boolean isAnonymous;
        private boolean isSerialized;
        private boolean isAuthorArticleReviewer;

        private ScoreInfo scoreInfo;
        private String articleType;
        private String thumbnail;
        private String createdAt;
        private String updatedAt;
        private String status;
        private boolean isLeetcode;
        private boolean canSee;
        private boolean canEdit;
        private boolean isMyFavorite;
        private String myReactionType;
        private String topicId;
        private int hitCount;

        private List<Reaction> reactions;
        private List<Tag> tags;
        private Topic topic;
    }

    public static class Author {
        private String realName;
        private String userAvatar;
        private String userSlug;
        private String userName;
        private String nameColor;
        private String certificationLevel;
        private ActiveBadge activeBadge;
    }

    public static class ActiveBadge {
        private String icon;
        private String displayName;
    }

    public static class ScoreInfo {
        private double scoreCoefficient;
    }

    public static class Reaction {
        private int count;
        private String reactionType;
    }

    public static class Tag {
        private String name;
        private String slug;
        private String tagType;
    }

    public static class Topic {
        private String id;
        private int topLevelCommentCount;
    }
}
