package yapspace;

import yapspace.exception.ValidationException;
import yapspace.model.AdminUser;
import yapspace.model.Category;
import yapspace.model.Comment;
import yapspace.model.Feeling;
import yapspace.model.Post;
import yapspace.model.Reaction;
import yapspace.model.ReactionType;
import yapspace.model.RegularUser;
import yapspace.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A small console demo that shows the foundation classes working together.
 * Run it to check the basic classes without the website.
 */
public class ConsoleDemo {

    public static void main(String[] args) {
        // Arrays/Data handling: ArrayList stores the objects
        List<User> users = new ArrayList<>();
        users.add(new RegularUser(1, "tellebyte", "telle@example.com", "salt", "hash", LocalDateTime.now()));
        users.add(new RegularUser(2, "mika", "mika@example.com", "salt", "hash", LocalDateTime.now()));
        users.add(new AdminUser(3, "admin", "admin@example.com", "salt", "hash", LocalDateTime.now()));

        // Classes and objects: a post, a comment and a reaction
        Post post = new Post(1, "tellebyte", "Finals week is draining me.", Category.SCHOOL, Feeling.STRESSED);
        Comment comment = new Comment(1, post.getId(), "mika", "You got this!");
        Reaction reaction = new Reaction(1, post.getId(), "mika", ReactionType.ENCOURAGE);

        System.out.println(post.getSummary());
        System.out.println(comment.getSummary());
        System.out.println(reaction.getSummary());
        System.out.println();

        // Polymorphism: same method call, different answer depending on the kind of user
        for (User user : users) {
            System.out.println(user.getSummary() + " can delete the post? " + user.canDelete(post));
        }
        System.out.println();

        // Encapsulation: edit through a method, not by touching fields
        post.update("Finals week is draining me, but I'm hanging in there.", Category.SCHOOL, Feeling.TIRED);
        System.out.println("Edited? " + post.isEdited() + " | Feeling now: " + post.getFeeling().getLabel());
        System.out.println();

        // Exception handling: invalid input is caught with try-catch
        try {
            Category.parse("");
        } catch (ValidationException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}
