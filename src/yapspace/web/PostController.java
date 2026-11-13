package yapspace.web;

import yapspace.exception.PermissionException;
import yapspace.exception.ValidationException;
import yapspace.model.Comment;
import yapspace.model.Post;
import yapspace.model.User;
import yapspace.service.PostService;

/** Feed, My Posts, creating / editing / deleting posts, reactions and comments. */
public class PostController {

    private final PostService posts;
    private final Views views;

    public PostController(PostService posts, Views views) {
        this.posts = posts;
        this.views = views;
    }

    public void addRoutes(Router router) {
        router.get("/feed", this::feed);
        router.get("/my-posts", this::myPosts);
        router.get("/posts/new", this::newForm);          // must come before /posts/{id}
        router.post("/posts", this::create);
        router.get("/posts/{id}", this::detail);
        router.get("/posts/{id}/edit", this::editForm);
        router.post("/posts/{id}/edit", this::editSubmit);
        router.post("/posts/{id}/delete", this::delete);
        router.post("/posts/{id}/react", this::react);
        router.post("/posts/{id}/comments", this::addComment);
        router.get("/comments/{id}/edit", this::commentEditForm);
        router.post("/comments/{id}/edit", this::commentEditSubmit);
        router.post("/comments/{id}/delete", this::commentDelete);
    }

    // ------------------------------------------------------------ lists

    private WebResponse feed(WebRequest req) {
        req.requireUser();
        return WebResponse.html(views.feed(req, posts.allPosts()));
    }

    private WebResponse myPosts(WebRequest req) {
        User user = req.requireUser();
        return WebResponse.html(views.myPosts(req, posts.postsBy(user.getUsername())));
    }

    // ------------------------------------------------------- create / edit

    private WebResponse newForm(WebRequest req) {
        req.requireUser();
        return WebResponse.html(views.postForm(req, null, null, "", "", ""));
    }

    private WebResponse create(WebRequest req) {
        User user = req.requireUser();
        try {
            posts.createPost(user, req.param("content"), req.param("category"), req.param("feeling"));
            req.getSession().setFlash("Your post is live!");
            return WebResponse.redirect("/feed");
        } catch (ValidationException e) {
            return WebResponse.html(400, views.postForm(req, null, e.getMessage(),
                    req.param("content"), req.param("category"), req.param("feeling")));
        }
    }

    private WebResponse detail(WebRequest req) {
        req.requireUser();
        Post post = posts.getPost(req.pathInt("id"));
        return WebResponse.html(views.postDetail(req, post, null, ""));
    }

    private WebResponse editForm(WebRequest req) {
        User user = req.requireUser();
        Post post = posts.getPost(req.pathInt("id"));
        if (!user.canEdit(post)) {
            throw new PermissionException("You can only edit your own posts.");
        }
        return WebResponse.html(views.postForm(req, post, null, post.getContent(),
                post.getCategory().name(), post.getFeeling().name()));
    }

    private WebResponse editSubmit(WebRequest req) {
        User user = req.requireUser();
        int id = req.pathInt("id");
        try {
            posts.editPost(user, id, req.param("content"), req.param("category"), req.param("feeling"));
            req.getSession().setFlash("Your post was updated.");
            return WebResponse.redirect("/my-posts");
        } catch (ValidationException e) {
            return WebResponse.html(400, views.postForm(req, posts.getPost(id), e.getMessage(),
                    req.param("content"), req.param("category"), req.param("feeling")));
        }
    }

    private WebResponse delete(WebRequest req) {
        User user = req.requireUser();
        posts.deletePost(user, req.pathInt("id"));
        req.getSession().setFlash("The post was deleted.");
        return WebResponse.redirect(Html.safeNext(req.param("next"), "/feed"));
    }

    // ----------------------------------------------------------- reactions

    private WebResponse react(WebRequest req) {
        User user = req.requireUser();
        int id = req.pathInt("id");
        String next = Html.safeNext(req.param("next"), "/feed");
        try {
            posts.reactToPost(user, id, req.param("type"));
        } catch (ValidationException e) {
            req.getSession().setErrorFlash(e.getMessage());
        }
        return WebResponse.redirect(next + "#post-" + id);
    }

    // ------------------------------------------------------------ comments

    private WebResponse addComment(WebRequest req) {
        User user = req.requireUser();
        int id = req.pathInt("id");
        try {
            posts.addComment(user, id, req.param("content"));
            return WebResponse.redirect("/posts/" + id + "#comments");
        } catch (ValidationException e) {
            return WebResponse.html(400, views.postDetail(req, posts.getPost(id), e.getMessage(), req.param("content")));
        }
    }

    private WebResponse commentEditForm(WebRequest req) {
        User user = req.requireUser();
        Comment comment = posts.getComment(req.pathInt("id"));
        if (!user.canEdit(comment)) {
            throw new PermissionException("You can only edit your own comments.");
        }
        return WebResponse.html(views.commentEdit(req, comment, null, comment.getContent()));
    }

    private WebResponse commentEditSubmit(WebRequest req) {
        User user = req.requireUser();
        int id = req.pathInt("id");
        try {
            Comment comment = posts.editComment(user, id, req.param("content"));
            req.getSession().setFlash("Your comment was updated.");
            return WebResponse.redirect("/posts/" + comment.getPostId() + "#comment-" + id);
        } catch (ValidationException e) {
            return WebResponse.html(400, views.commentEdit(req, posts.getComment(id), e.getMessage(), req.param("content")));
        }
    }

    private WebResponse commentDelete(WebRequest req) {
        User user = req.requireUser();
        posts.deleteComment(user, req.pathInt("id"));
        req.getSession().setFlash("The comment was deleted.");
        return WebResponse.redirect(Html.safeNext(req.param("next"), "/feed") + "#comments");
    }
}
