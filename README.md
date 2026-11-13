# YapSpace

A simple Java web app where people aged 13 and above can share thoughts and feelings, react and comment.
Made for OOP 101 (Team 4, Ro Ro Row Your Code).

## What it does

- Sign up and log in (13+ confirmation, passwords are stored hashed, never as plain text)
- Community feed with everyone's posts, newest first
- Write a post with a **category** and a **feeling**
- React to posts (Support, Relate, Encourage) and leave comments
- **My Posts**: see, edit and delete your own posts
- Edit or delete your own comments

## Run it in VS Code

1. Install Java 17 or newer (check with `java -version` in a terminal).
2. In VS Code, install **Extension Pack for Java** (by Microsoft).
3. File > Open Folder, and choose this `YapSpace` folder (the one with `src` inside).
4. Open `src/yapspace/Main.java` and click **Run** above `public static void main`.
5. Open **http://localhost:8080** in your browser.

Press the stop button (or Ctrl+C) to stop the server.

### Or run it from a terminal

Mac / Linux:
```
javac -d out $(find src -name '*.java')
java -cp out yapspace.Main
```

Windows (PowerShell):
```
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out yapspace.Main
```

If port 8080 is busy, add `--port=8081` to the last command and open that port instead.

## Where is the data?

Everything is saved as text files in the `data` folder (created on first run), so posts are still there
after you restart. To start fresh, stop the program and delete the `data` folder.

The site only opens on your own computer (localhost).

## How the code is organized

```
src/yapspace/
  Main.java            starts the program
  ConsoleDemo.java     tiny demo of the basic classes (no website)
  model/               User, RegularUser, AdminUser, Post, Comment, Reaction, and the enums
  exception/           ValidationException, AuthenticationException, PermissionException, NotFoundException
  service/             UserService (register, login), PostService (posts, comments, reactions), Validator, PasswordHasher
  storage/             DataStore (ArrayLists + saving to the data folder)
  web/                 Router, controllers, Views (the HTML pages), Assets (CSS and JS)
```

## Where each OOP concept from the proposal is used

| Concept | Where |
|---|---|
| Classes and objects | `User`, `Post`, `Comment`, `Reaction`, `Category` |
| Methods | `register()`, `login()`, `createPost()`, `editPost()`, `deletePost()`, `addComment()`, `reactToPost()` in the `service` package |
| Encapsulation | private fields with getters/setters in every model class |
| Inheritance | `RegularUser` and `AdminUser` extend `User`; every model extends `BaseEntity` |
| Abstraction | abstract `BaseEntity` and `User`; `PasswordHasher` hides how passwords are secured |
| Polymorphism | `user.canDelete(post)` answers differently for `RegularUser` and `AdminUser` |
| Exception handling | `try-catch` in the controllers and `Router` turns exceptions into friendly messages |
| Arrays/Data handling | `DataStore` keeps users, posts, comments and reactions in `ArrayList`s |

## Not included (kept simple on purpose)

Search, admin dashboard, reporting posts, images, and private messages. The `AdminUser` class exists
to show polymorphism, but the website does not create an admin account.
