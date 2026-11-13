package yapspace.service;

import yapspace.exception.NotFoundException;
import yapspace.exception.PermissionException;
import yapspace.exception.ValidationException;
import yapspace.model.Category;
import yapspace.model.Comment;
import yapspace.model.Feeling;
import yapspace.model.Post;
import yapspace.model.Reaction;
import yapspace.model.ReactionType;
import yapspace.model.User;
import yapspace.storage.DataStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Everything about posts, comments and reactions: createPost(), editPost(),
 * deletePost(), addComment(), editComment(), deleteComment(), reactToPost()
 * and removeReaction() from the proposal.
 *
 * Permission rules are asked from the user object (user.canDelete(post)), so
 * polymorphism gives the right answer for a RegularUser or an AdminUser.
 */
public class PostService {

    private final DataStore store;

    public PostService(DataStore store) {
        this.store = store;
    }

    // ------------------------------------------------------------------ posts

    public Post createPost(User author, String content, String categoryCode, String feelingCode) {
        String text = Validator.postContent(content);
        Category category = Category.parse(categoryCode);
        Feeling feeling = Feeling.parse(feelingCode);
        synchronized (store) {
            Post post = new Post(store.nextPostId(), author.getUsername(), text, category, feeling);
            store.posts().add(post);
            store.savePosts();
            return post;
        }
    }

    public Post editPost(User editor, int postId, String content, String categoryCode, String feelingCode) {
        String text = Validator.postContent(content);
        Category category = Category.parse(categoryCode);
        Feeling feeling = Feeling.parse(feelingCode);
        synchronized (store) {
            Post post = getPost(postId);
            if (!editor.canEdit(post)) {
                throw new PermissionException("You can only edit your own posts.");
            }
            post.update(text, category, feeling);
            store.savePosts();
            return post;
        }
    }

    /** Deletes a post together with its comments and reactions. */
    public void deletePost(User actor, int postId) {
        synchronized (store) {
            Post post = getPost(postId);
            if (!actor.canDelete(post)) {
                throw new PermissionException("You can only delete your own posts.");
            }
            store.posts().remove(post);
            store.comments().removeIf(c -> c.getPostId() == postId);
            store.reactions().removeIf(r -> r.getPostId() == postId);
            store.savePosts();
            store.saveComments();
            store.saveReactions();
        }
    }

    public Post getPost(int postId) {
        synchronized (store) {
            for (Post p : store.posts()) {
                if (p.getId() == postId) {
                    return p;
                }
            }
        }
        throw new NotFoundException("That post does not exist (it may have been deleted).");
    }

    /** The community feed: every post, newest first. */
    public List<Post> allPosts() {
        List<Post> list;
        synchronized (store) {
            list = new ArrayList<>(store.posts());
        }
        list.sort(newestFirst());
        return list;
    }

    /** The "My Posts" section: only this user's posts, newest first. */
    public List<Post> postsBy(String username) {
        List<Post> mine = new ArrayList<>();
        synchronized (store) {
            for (Post p : store.posts()) {
                if (p.isOwnedBy(username)) {
                    mine.add(p);
                }
            }
        }
        mine.sort(newestFirst());
        return mine;
    }

    private static Comparator<Post> newestFirst() {
        return Comparator.comparing(Post::getCreatedAt).thenComparingInt(Post::getId).reversed();
    }

    // -------------------------------------------------------------- reactions

    /**
     * First click adds the reaction, the same reaction again removes it,
     * a different reaction switches it. Returns the reaction afterwards (null if removed).
     */
    public ReactionType reactToPost(User user, int postId, String reactionCode) {
        ReactionType type = ReactionType.parse(reactionCode);
        synchronized (store) {
            getPost(postId);
            Reaction existing = findReaction(postId, user.getUsername());
            if (existing == null) {
                return addReaction(user, postId, type);
            }
            if (existing.getType() == type) {
                removeReaction(user, postId);
                return null;
            }
            existing.setType(type);
            store.saveReactions();
            return type;
        }
    }

    public ReactionType addReaction(User user, int postId, ReactionType type) {
        synchronized (store) {
            getPost(postId);
            if (findReaction(postId, user.getUsername()) != null) {
                throw new ValidationException("You already reacted to this post.");
            }
            store.reactions().add(new Reaction(store.nextReactionId(), postId, user.getUsername(), type));
            store.saveReactions();
            return type;
        }
    }

    public void removeReaction(User user, int postId) {
        synchronized (store) {
            Reaction existing = findReaction(postId, user.getUsername());
            if (existing != null) {
                store.reactions().remove(existing);
                store.saveReactions();
            }
        }
    }

    private Reaction findReaction(int postId, String username) {
        for (Reaction r : store.reactions()) {
            if (r.getPostId() == postId && r.getUsername().equalsIgnoreCase(username)) {
                return r;
            }
        }
        return null;
    }

    public ReactionType reactionOf(int postId, String username) {
        synchronized (store) {
            Reaction r = findReaction(postId, username);
            return r == null ? null : r.getType();
        }
    }

    public Map<ReactionType, Integer> reactionCounts(int postId) {
        Map<ReactionType, Integer> counts = new EnumMap<>(ReactionType.class);
        for (ReactionType t : ReactionType.values()) {
            counts.put(t, 0);
        }
        synchronized (store) {
            for (Reaction r : store.reactions()) {
                if (r.getPostId() == postId) {
                    counts.merge(r.getType(), 1, Integer::sum);
                }
            }
        }
        return counts;
    }

    // --------------------------------------------------------------- comments

    public Comment addComment(User author, int postId, String content) {
        String text = Validator.commentContent(content);
        synchronized (store) {
            getPost(postId);
            Comment comment = new Comment(store.nextCommentId(), postId, author.getUsername(), text);
            store.comments().add(comment);
            store.saveComments();
            return comment;
        }
    }

    public Comment editComment(User editor, int commentId, String content) {
        String text = Validator.commentContent(content);
        synchronized (store) {
            Comment comment = getComment(commentId);
            if (!editor.canEdit(comment)) {
                throw new PermissionException("You can only edit your own comments.");
            }
            comment.edit(text);
            store.saveComments();
            return comment;
        }
    }

    public void deleteComment(User actor, int commentId) {
        synchronized (store) {
            Comment comment = getComment(commentId);
            if (!actor.canDelete(comment)) {
                throw new PermissionException("You can only delete your own comments.");
            }
            store.comments().remove(comment);
            store.saveComments();
        }
    }

    public Comment getComment(int commentId) {
        synchronized (store) {
            for (Comment c : store.comments()) {
                if (c.getId() == commentId) {
                    return c;
                }
            }
        }
        throw new NotFoundException("That comment does not exist (it may have been deleted).");
    }

    /** Comments on a post, oldest first, so the conversation reads in order. */
    public List<Comment> commentsFor(int postId) {
        List<Comment> list = new ArrayList<>();
        synchronized (store) {
            for (Comment c : store.comments()) {
                if (c.getPostId() == postId) {
                    list.add(c);
                }
            }
        }
        list.sort(Comparator.comparing(Comment::getCreatedAt).thenComparingInt(Comment::getId));
        return list;
    }

    public int commentCount(int postId) {
        return commentsFor(postId).size();
    }
}
