package yapspace.web;

import yapspace.model.Category;
import yapspace.model.Comment;
import yapspace.model.Feeling;
import yapspace.model.Post;
import yapspace.model.ReactionType;
import yapspace.model.User;
import yapspace.service.PostService;
import yapspace.service.Validator;

import java.util.List;
import java.util.Map;

/**
 * Builds the HTML of every page. Anything a user typed is passed through
 * Html.esc(...) before it is placed in a page, so it can never run as code.
 */
public class Views {

    private enum CardMode { FEED, MINE, DETAIL }

    private final PostService posts;

    public Views(PostService posts) {
        this.posts = posts;
    }

    private static String esc(String text) {
        return Html.esc(text);
    }

    private static String hidden(String name, String value) {
        return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + esc(value) + "\">";
    }

    private static String plural(int n, String word) {
        return n + " " + word + (n == 1 ? "" : "s");
    }

    // ------------------------------------------------------------------ layout

    private String layout(WebRequest req, String title, String active, String width, String body) {
        User me = req.getUser();
        StringBuilder sb = new StringBuilder(6000);
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<meta name=\"color-scheme\" content=\"light dark\">")
                .append("<title>").append(esc(title)).append(" &middot; YapSpace</title>")
                .append("<link rel=\"icon\" href=\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' ")
                .append("viewBox='0 0 100 100'%3E%3Ctext y='.9em' font-size='90'%3E%F0%9F%92%AC%3C/text%3E%3C/svg%3E\">")
                .append("<link rel=\"stylesheet\" href=\"/static/style.css\"></head><body>")
                .append("<a class=\"skip\" href=\"#main\">Skip to content</a>");

        sb.append("<header class=\"topbar\"><div class=\"topbar-in\">")
                .append("<a class=\"brand\" href=\"").append(me == null ? "/" : "/feed").append("\">&#128172; Yap<span>Space</span></a>");
        if (me != null) {
            sb.append("<nav class=\"nav\" aria-label=\"Main\">")
                    .append(navLink("/feed", "Feed", "feed".equals(active)))
                    .append(navLink("/my-posts", "My Posts", "mine".equals(active)))
                    .append("</nav><div class=\"topbar-right\">")
                    .append("<a class=\"btn btn-primary btn-sm\" href=\"/posts/new\">+ New post</a>")
                    .append("<span class=\"whoami\">@").append(esc(me.getUsername())).append("</span>")
                    .append("<form class=\"inline\" method=\"post\" action=\"/logout\">")
                    .append("<button class=\"btn btn-ghost btn-sm\" type=\"submit\">Log out</button></form></div>");
        } else {
            sb.append("<nav class=\"nav\" aria-label=\"Main\"></nav><div class=\"topbar-right\">")
                    .append("<a class=\"btn btn-ghost btn-sm\" href=\"/login\">Log in</a>")
                    .append("<a class=\"btn btn-primary btn-sm\" href=\"/register\">Sign up</a></div>");
        }
        sb.append("</div></header>");

        sb.append("<main id=\"main\" class=\"wrap ").append(width).append("\">");
        Session session = req.getSession();
        Session.Flash flash = session == null ? null : session.takeFlash();
        if (flash != null) {
            sb.append("<div class=\"alert ").append(flash.error() ? "alert-error\" role=\"alert\"" : "alert-ok\" role=\"status\"")
                    .append(">").append(esc(flash.message())).append("</div>");
        }
        sb.append(body).append("</main>");

        sb.append("<footer class=\"footer\"><p>YapSpace is a place for general community sharing. It does not provide ")
                .append("professional counseling, medical advice or emergency services. If you or someone you know is in ")
                .append("danger, contact your local emergency services or a trusted adult right away.</p>")
                .append("<p>OOP 101 project &middot; Team 4, Ro Ro Row Your Code</p></footer>")
                .append("<script src=\"/static/app.js\" defer></script></body></html>");
        return sb.toString();
    }

    private static String navLink(String href, String label, boolean active) {
        return "<a href=\"" + href + "\"" + (active ? " class=\"active\" aria-current=\"page\"" : "") + ">" + label + "</a>";
    }

    private static String errorBox(String message) {
        return message == null || message.isEmpty() ? ""
                : "<div class=\"alert alert-error\" role=\"alert\">" + esc(message) + "</div>";
    }

    private static String avatar(String username, boolean small) {
        return "<span class=\"avatar " + (small ? "sm " : "") + "a" + Html.avatarSlot(username) + "\" aria-hidden=\"true\">"
                + esc(Html.initial(username)) + "</span>";
    }

    // ------------------------------------------------------------ public pages

    public String landing(WebRequest req) {
        StringBuilder b = new StringBuilder();
        b.append("<section class=\"hero\"><span class=\"eyebrow\">A community space for ages 13 and up</span>")
                .append("<h1>Say what&rsquo;s on your <em>mind</em>.</h1>")
                .append("<p class=\"lead\">A simple, friendly place to share your thoughts, feelings and experiences, ")
                .append("and to find people who relate.</p><div class=\"hero-cta\">");
        if (req.isLoggedIn()) {
            b.append("<a class=\"btn btn-primary btn-lg\" href=\"/feed\">Go to the feed</a>");
        } else {
            b.append("<a class=\"btn btn-primary btn-lg\" href=\"/register\">Join YapSpace</a>")
                    .append("<a class=\"btn btn-ghost btn-lg\" href=\"/login\">Log in</a>");
        }
        b.append("</div></section><section class=\"grid3\">")
                .append(featureCard("&#9997;&#65039;", "Say it your way",
                        "Write a post, pick a category and tell everyone how you feel."))
                .append(featureCard("&#128155;", "Feel heard",
                        "React with Support, Relate or Encourage so nobody feels alone."))
                .append(featureCard("&#128172;", "Talk it through",
                        "Leave a kind comment and keep the conversation going."))
                .append("</section>");
        return layout(req, "Welcome", "", "wide", b.toString());
    }

    private static String featureCard(String icon, String title, String text) {
        return "<div class=\"card\"><span class=\"icon\" aria-hidden=\"true\">" + icon + "</span><h2>" + title
                + "</h2><p class=\"muted\">" + text + "</p></div>";
    }

    public String register(WebRequest req, String error, String username, String email, boolean ageChecked) {
        String body = "<div class=\"card\"><h1>Create your account</h1>"
                + "<p class=\"muted\">YapSpace is for people aged 13 and above.</p>" + errorBox(error)
                + "<form method=\"post\" action=\"/register\">"
                + "<div class=\"field\"><label for=\"username\">Username</label>"
                + "<input type=\"text\" id=\"username\" name=\"username\" value=\"" + esc(username) + "\" required minlength=\"3\" "
                + "maxlength=\"20\" pattern=\"[A-Za-z0-9_]{3,20}\" autocomplete=\"username\">"
                + "<div class=\"hint\">3-20 letters, numbers or underscores.</div></div>"
                + "<div class=\"field\"><label for=\"email\">Email</label>"
                + "<input type=\"email\" id=\"email\" name=\"email\" value=\"" + esc(email) + "\" required maxlength=\"100\" autocomplete=\"email\"></div>"
                + "<div class=\"field\"><label for=\"password\">Password</label>"
                + "<input type=\"password\" id=\"password\" name=\"password\" required minlength=\"" + Validator.PASSWORD_MIN
                + "\" maxlength=\"" + Validator.PASSWORD_MAX + "\" autocomplete=\"new-password\">"
                + "<div class=\"hint\">At least " + Validator.PASSWORD_MIN + " characters.</div></div>"
                + "<div class=\"field\"><label for=\"confirm\">Confirm password</label>"
                + "<input type=\"password\" id=\"confirm\" name=\"confirm\" required autocomplete=\"new-password\"></div>"
                + "<div class=\"field\"><label class=\"check\"><input type=\"checkbox\" name=\"age\" value=\"yes\" required"
                + (ageChecked ? " checked" : "") + "> I am 13 years old or above.</label></div>"
                + "<button class=\"btn btn-primary\" type=\"submit\">Create account</button></form>"
                + "<p class=\"muted form-actions\">Already have an account? <a href=\"/login\">Log in</a></p></div>";
        return layout(req, "Sign up", "", "narrow", body);
    }

    public String login(WebRequest req, String error, String username) {
        String body = "<div class=\"card\"><h1>Welcome back</h1>" + errorBox(error)
                + "<form method=\"post\" action=\"/login\">"
                + "<div class=\"field\"><label for=\"username\">Username</label>"
                + "<input type=\"text\" id=\"username\" name=\"username\" value=\"" + esc(username)
                + "\" required maxlength=\"20\" autocomplete=\"username\"></div>"
                + "<div class=\"field\"><label for=\"password\">Password</label>"
                + "<input type=\"password\" id=\"password\" name=\"password\" required autocomplete=\"current-password\"></div>"
                + "<button class=\"btn btn-primary\" type=\"submit\">Log in</button></form>"
                + "<p class=\"muted form-actions\">New here? <a href=\"/register\">Create an account</a></p></div>";
        return layout(req, "Log in", "", "narrow", body);
    }

    // ------------------------------------------------------------------- feed

    public String feed(WebRequest req, List<Post> list) {
        StringBuilder b = new StringBuilder();
        b.append("<div class=\"page-head\"><h1>Community Feed</h1><span class=\"muted\">")
                .append(plural(list.size(), "post")).append("</span></div>");
        if (list.isEmpty()) {
            b.append("<div class=\"card empty\"><span class=\"icon\" aria-hidden=\"true\">&#128173;</span>")
                    .append("<p>It&rsquo;s quiet here. Be the first to share something!</p>")
                    .append("<a class=\"btn btn-primary\" href=\"/posts/new\">Write a post</a></div>");
        }
        for (Post p : list) {
            b.append(postCard(req, p, CardMode.FEED));
        }
        return layout(req, "Community Feed", "feed", "", b.toString());
    }

    public String myPosts(WebRequest req, List<Post> mine) {
        StringBuilder b = new StringBuilder();
        b.append("<div class=\"page-head\"><h1>My Posts</h1><span class=\"muted\">").append(plural(mine.size(), "post")).append("</span></div>");
        if (mine.isEmpty()) {
            b.append("<div class=\"card empty\"><span class=\"icon\" aria-hidden=\"true\">&#128221;</span>")
                    .append("<p>You haven&rsquo;t posted anything yet.</p><a class=\"btn btn-primary\" href=\"/posts/new\">Write your first post</a></div>");
        }
        for (Post p : mine) {
            b.append(postCard(req, p, CardMode.MINE));
        }
        return layout(req, "My Posts", "mine", "", b.toString());
    }

    // ------------------------------------------------------------- post cards

    private String postCard(WebRequest req, Post p, CardMode mode) {
        User me = req.getUser();
        int id = p.getId();
        String next = switch (mode) {
            case FEED -> "/feed";
            case MINE -> "/my-posts";
            case DETAIL -> "/posts/" + id;
        };
        String afterDelete = mode == CardMode.MINE ? "/my-posts" : "/feed";

        StringBuilder sb = new StringBuilder();
        sb.append("<article class=\"card post\" id=\"post-").append(id).append("\"><header class=\"post-head\">")
                .append(avatar(p.getAuthor(), false))
                .append("<div class=\"post-who\"><strong>").append(esc(p.getAuthor())).append("</strong><span class=\"muted\">")
                .append(Html.timeTag(p.getCreatedAt())).append(p.isEdited() ? " &middot; edited" : "").append("</span></div>")
                .append("<span class=\"feeling-tag\">").append(p.getFeeling().getIcon()).append(' ')
                .append(esc(p.getFeeling().getLabel())).append("</span></header>")
                .append("<p class=\"post-body\">").append(Html.multiline(p.getContent())).append("</p>")
                .append("<div class=\"post-meta\"><span class=\"chip\">").append(esc(p.getCategory().getLabel()))
                .append("</span></div><div class=\"post-actions\">");

        ReactionType mine = posts.reactionOf(id, me.getUsername());
        Map<ReactionType, Integer> counts = posts.reactionCounts(id);
        for (ReactionType t : ReactionType.values()) {
            boolean active = t == mine;
            sb.append("<form class=\"inline\" method=\"post\" action=\"/posts/").append(id).append("/react\">")
                    .append(hidden("type", t.name())).append(hidden("next", next))
                    .append("<button class=\"react-btn").append(active ? " active" : "").append("\" type=\"submit\" aria-pressed=\"")
                    .append(active).append("\">").append(t.getIcon()).append(' ').append(esc(t.getLabel())).append(' ')
                    .append(counts.get(t)).append("</button></form>");
        }
        sb.append("<span class=\"spacer\"></span>");

        if (mode != CardMode.DETAIL) {
            sb.append("<a class=\"link-btn\" href=\"/posts/").append(id).append("#comments\">")
                    .append(plural(posts.commentCount(id), "comment")).append("</a>");
        }
        if (mode != CardMode.FEED && me.canEdit(p)) {
            sb.append("<a class=\"link-btn\" href=\"/posts/").append(id).append("/edit\">Edit</a>");
        }
        if (mode != CardMode.FEED && me.canDelete(p)) {
            sb.append(deleteForm("/posts/" + id + "/delete", afterDelete, "Delete this post? This cannot be undone."));
        }
        sb.append("</div></article>");
        return sb.toString();
    }

    private static String deleteForm(String action, String next, String confirmMessage) {
        return "<form class=\"inline\" method=\"post\" action=\"" + action + "\" data-confirm=\"" + esc(confirmMessage) + "\">"
                + hidden("next", next) + "<button class=\"link-btn danger\" type=\"submit\">Delete</button></form>";
    }

    // ------------------------------------------------------------ post screens

    /** The create / edit form. Pass the existing post when editing, otherwise null. */
    public String postForm(WebRequest req, Post existing, String error, String content, String category, String feeling) {
        boolean editing = existing != null;
        String action = editing ? "/posts/" + existing.getId() + "/edit" : "/posts";
        StringBuilder b = new StringBuilder();
        b.append("<div class=\"card\"><h1>").append(editing ? "Edit your post" : "New post").append("</h1>")
                .append(errorBox(error)).append("<form method=\"post\" action=\"").append(action).append("\">")
                .append("<div class=\"field\"><label for=\"content\">What&rsquo;s on your mind?</label>")
                .append("<textarea id=\"content\" name=\"content\" maxlength=\"").append(Validator.POST_MAX)
                .append("\" required data-counter=\"count\" placeholder=\"Say it however you like&hellip;\">")
                .append(esc(content)).append("</textarea><div class=\"counter\" id=\"count\"></div></div>")
                .append("<div class=\"field\"><label for=\"category\">Category</label><select id=\"category\" name=\"category\" required>")
                .append("<option value=\"\" disabled").append(category == null || category.isEmpty() ? " selected" : "")
                .append(">Choose a category</option>");
        for (Category c : Category.values()) {
            b.append("<option value=\"").append(c.name()).append("\"").append(c.name().equals(category) ? " selected" : "")
                    .append(">").append(esc(c.getLabel())).append("</option>");
        }
        b.append("</select></div><fieldset><legend>How are you feeling?</legend><div class=\"feelings\">");
        for (Feeling f : Feeling.values()) {
            b.append("<label class=\"feeling-opt\"><input class=\"sr-only\" type=\"radio\" name=\"feeling\" value=\"")
                    .append(f.name()).append("\" required").append(f.name().equals(feeling) ? " checked" : "").append("><span>")
                    .append(f.getIcon()).append(' ').append(esc(f.getLabel())).append("</span></label>");
        }
        b.append("</div></fieldset><div class=\"form-actions\"><button class=\"btn btn-primary\" type=\"submit\">")
                .append(editing ? "Save changes" : "Post").append("</button><a class=\"btn btn-ghost\" href=\"")
                .append(editing ? "/my-posts" : "/feed").append("\">Cancel</a></div></form></div>");
        return layout(req, editing ? "Edit post" : "New post", editing ? "mine" : "", "", b.toString());
    }

    public String postDetail(WebRequest req, Post post, String commentError, String commentDraft) {
        User me = req.getUser();
        StringBuilder b = new StringBuilder();
        b.append("<p><a href=\"/feed\">&larr; Back to the feed</a></p>").append(postCard(req, post, CardMode.DETAIL));

        List<Comment> comments = posts.commentsFor(post.getId());
        b.append("<section class=\"card\" id=\"comments\"><h2>").append(plural(comments.size(), "comment")).append("</h2>");
        if (comments.isEmpty()) {
            b.append("<p class=\"muted\">No comments yet. Say something kind!</p>");
        } else {
            b.append("<ul class=\"comments\">");
            for (Comment c : comments) {
                b.append("<li class=\"comment\" id=\"comment-").append(c.getId()).append("\">")
                        .append(avatar(c.getAuthor(), true)).append("<div class=\"comment-body\"><strong>")
                        .append(esc(c.getAuthor())).append("</strong> <span class=\"muted\">").append(Html.timeTag(c.getCreatedAt()))
                        .append(c.isEdited() ? " &middot; edited" : "").append("</span><p>").append(Html.multiline(c.getContent())).append("</p>");
                if (me.canEdit(c)) {
                    b.append("<a class=\"link-btn\" href=\"/comments/").append(c.getId()).append("/edit\">Edit</a>");
                }
                if (me.canDelete(c)) {
                    b.append(deleteForm("/comments/" + c.getId() + "/delete", "/posts/" + post.getId(), "Delete this comment?"));
                }
                b.append("</div></li>");
            }
            b.append("</ul>");
        }
        b.append(errorBox(commentError)).append("<form method=\"post\" action=\"/posts/").append(post.getId()).append("/comments\">")
                .append("<div class=\"field\"><label for=\"comment\" class=\"sr-only\">Write a comment</label>")
                .append("<textarea class=\"short\" id=\"comment\" name=\"content\" maxlength=\"").append(Validator.COMMENT_MAX)
                .append("\" required data-counter=\"ccount\" placeholder=\"Write a kind comment&hellip;\">").append(esc(commentDraft))
                .append("</textarea><div class=\"counter\" id=\"ccount\"></div></div>")
                .append("<button class=\"btn btn-primary btn-sm\" type=\"submit\">Comment</button></form></section>");
        return layout(req, "Post by " + post.getAuthor(), "feed", "", b.toString());
    }

    public String commentEdit(WebRequest req, Comment comment, String error, String draft) {
        String body = "<div class=\"card\"><h1>Edit your comment</h1>" + errorBox(error)
                + "<form method=\"post\" action=\"/comments/" + comment.getId() + "/edit\">"
                + "<div class=\"field\"><label for=\"content\" class=\"sr-only\">Comment</label>"
                + "<textarea class=\"short\" id=\"content\" name=\"content\" maxlength=\"" + Validator.COMMENT_MAX
                + "\" required data-counter=\"ccount\">" + esc(draft) + "</textarea><div class=\"counter\" id=\"ccount\"></div></div>"
                + "<div class=\"form-actions\"><button class=\"btn btn-primary\" type=\"submit\">Save changes</button>"
                + "<a class=\"btn btn-ghost\" href=\"/posts/" + comment.getPostId() + "#comment-" + comment.getId() + "\">Cancel</a></div></form></div>";
        return layout(req, "Edit comment", "feed", "", body);
    }

    // ------------------------------------------------------------------ errors

    public WebResponse error(WebRequest req, int status, String title, String message) {
        String body = "<div class=\"card empty\"><span class=\"icon\" aria-hidden=\"true\">&#128533;</span><h1>" + esc(title)
                + "</h1><p>" + esc(message) + "</p><a class=\"btn btn-primary\" href=\""
                + (req.isLoggedIn() ? "/feed" : "/") + "\">Go back home</a></div>";
        return WebResponse.html(status, layout(req, title, "", "", body));
    }
}
