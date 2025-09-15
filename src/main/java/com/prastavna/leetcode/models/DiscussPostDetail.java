package com.prastavna.leetcode.models;

import java.util.List;

public class DiscussPostDetail {
    public UgcArticleDiscussionArticle ugcArticleDiscussionArticle;

    public static class UgcArticleDiscussionArticle {
        public String uuid;
        public String title;
        public String slug;
        public String summary;
        public String content;
        public boolean isSlate;

        public Author author;
        public boolean isOwner;
        public boolean isAnonymous;
        public boolean isSerialized;
        public boolean isAuthorArticleReviewer;

        public ScoreInfo scoreInfo;
        public String articleType;
        public String thumbnail;
        public String createdAt;
        public String updatedAt;
        public String status;
        public boolean isLeetcode;
        public boolean canSee;
        public boolean canEdit;
        public boolean isMyFavorite;
        public String myReactionType;
        public String topicId;
        public int hitCount;

        public List<Reaction> reactions;
        public List<Tag> tags;
        public Topic topic;
    }

    public static class Author {
        public String realName;
        public String userAvatar;
        public String userSlug;
        public String userName;
        public String nameColor;
        public String certificationLevel;
        public ActiveBadge activeBadge;
    }

    public static class ActiveBadge {
        public String icon;
        public String displayName;
    }

    public static class ScoreInfo {
        public double scoreCoefficient;
    }

    public static class Reaction {
        public int count;
        public String reactionType;
    }

    public static class Tag {
        public String name;
        public String slug;
        public String tagType;
    }

    public static class Topic {
        public String id;
        public int topLevelCommentCount;
    }
}
