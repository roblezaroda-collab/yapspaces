package yapspace.web;

/**
 * The website's stylesheet and script, kept inside the program so there are no
 * extra files to lose. Served at /static/style.css and /static/app.js.
 * (No inline styles or scripts anywhere: the browser is told to refuse them.)
 */
public final class Assets {

    private Assets() {
    }

    public static final String CSS = """
            :root {
              --bg: #f6f4fb;
              --surface: #ffffff;
              --surface-2: #f1eefb;
              --text: #221f33;
              --muted: #6b6785;
              --primary: #6c4df5;
              --primary-dark: #5538d6;
              --primary-ink: #ffffff;
              --primary-soft: #ece8ff;
              --accent: #0e9f93;
              --danger: #c92a62;
              --danger-soft: #fde8f0;
              --ok-soft: #e0f7f4;
              --border: #e4dff3;
              --shadow: 0 1px 2px rgba(34,31,51,.05), 0 8px 24px rgba(34,31,51,.06);
              --radius: 16px;
            }
            @media (prefers-color-scheme: dark) {
              :root {
                --bg: #14121f;
                --surface: #1e1b2f;
                --surface-2: #28243f;
                --text: #ece9fb;
                --muted: #a8a4c4;
                --primary: #9d8cff;
                --primary-dark: #b4a7ff;
                --primary-ink: #14121f;
                --primary-soft: #2d2755;
                --accent: #3ccfc0;
                --danger: #ff7aa6;
                --danger-soft: #3d2030;
                --ok-soft: #17332f;
                --border: #322e4d;
                --shadow: 0 1px 2px rgba(0,0,0,.3), 0 8px 24px rgba(0,0,0,.25);
              }
            }
            * { box-sizing: border-box; }
            html { -webkit-text-size-adjust: 100%; }
            body {
              margin: 0;
              background: var(--bg);
              color: var(--text);
              font: 16px/1.55 system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
              min-height: 100vh;
              display: flex;
              flex-direction: column;
            }
            a { color: var(--primary-dark); }
            h1, h2, h3 { line-height: 1.2; margin: 0 0 .5rem; letter-spacing: -0.01em; }
            h1 { font-size: 1.9rem; }
            h2 { font-size: 1.3rem; }
            p { margin: 0 0 1rem; }
            .muted { color: var(--muted); font-size: .9rem; }
            .sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); white-space: nowrap; }
            .skip { position: absolute; left: -999px; top: 0; background: var(--primary); color: var(--primary-ink); padding: .5rem 1rem; z-index: 50; }
            .skip:focus { left: 8px; top: 8px; }
            :focus-visible { outline: 3px solid var(--primary); outline-offset: 2px; }

            .topbar { background: var(--surface); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 10; }
            .topbar-in { max-width: 960px; margin: 0 auto; padding: .6rem 16px; display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; }
            .brand { font-weight: 800; font-size: 1.25rem; color: var(--text); text-decoration: none; letter-spacing: -0.02em; }
            .brand span { color: var(--primary); }
            .nav { display: flex; gap: .25rem; flex: 1; flex-wrap: wrap; }
            .nav a { padding: .4rem .8rem; border-radius: 999px; text-decoration: none; color: var(--muted); font-weight: 600; font-size: .95rem; }
            .nav a:hover { background: var(--surface-2); color: var(--text); }
            .nav a.active { background: var(--primary-soft); color: var(--primary-dark); }
            .topbar-right { display: flex; align-items: center; gap: .6rem; flex-wrap: wrap; }
            .whoami { font-size: .9rem; color: var(--muted); }
            form.inline { display: inline; margin: 0; }

            .wrap { width: 100%; max-width: 720px; margin: 0 auto; padding: 24px 16px 48px; flex: 1; }
            .wrap.wide { max-width: 960px; }
            .wrap.narrow { max-width: 460px; }
            .page-head { display: flex; align-items: center; justify-content: space-between; gap: 1rem; flex-wrap: wrap; margin-bottom: 1rem; }
            .page-head h1 { margin: 0; }

            .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); box-shadow: var(--shadow); padding: 1.1rem 1.2rem; margin-bottom: 1rem; }

            .btn { display: inline-flex; align-items: center; justify-content: center; gap: .4rem; border: 1px solid transparent; border-radius: 999px; padding: .6rem 1.2rem; font: inherit; font-weight: 700; cursor: pointer; text-decoration: none; line-height: 1.2; transition: transform .08s ease, background .15s ease; }
            .btn:active { transform: translateY(1px); }
            .btn-primary { background: var(--primary); color: var(--primary-ink); }
            .btn-primary:hover { background: var(--primary-dark); }
            .btn-ghost { background: transparent; color: var(--text); border-color: var(--border); }
            .btn-ghost:hover { background: var(--surface-2); }
            .btn-danger { background: transparent; color: var(--danger); border-color: var(--danger); }
            .btn-danger:hover { background: var(--danger-soft); }
            .btn-sm { padding: .35rem .8rem; font-size: .875rem; }
            .btn-lg { padding: .85rem 1.6rem; font-size: 1.05rem; }

            .alert { border-radius: 12px; padding: .75rem 1rem; margin-bottom: 1rem; font-weight: 600; }
            .alert-ok { background: var(--ok-soft); color: var(--accent); border: 1px solid var(--accent); }
            .alert-error { background: var(--danger-soft); color: var(--danger); border: 1px solid var(--danger); }

            .field { margin-bottom: 1rem; }
            .field label, legend { display: block; font-weight: 700; margin-bottom: .3rem; font-size: .95rem; }
            input[type=text], input[type=email], input[type=password], input[type=search], select, textarea {
              width: 100%; font: inherit; color: var(--text); background: var(--surface); border: 1.5px solid var(--border);
              border-radius: 12px; padding: .65rem .85rem;
            }
            textarea { min-height: 140px; resize: vertical; }
            textarea.short { min-height: 80px; }
            input:focus, select:focus, textarea:focus { border-color: var(--primary); outline: none; box-shadow: 0 0 0 3px var(--primary-soft); }
            .hint { color: var(--muted); font-size: .85rem; margin-top: .25rem; }
            .counter { text-align: right; color: var(--muted); font-size: .85rem; }
            .check { display: flex; gap: .6rem; align-items: flex-start; font-weight: 500; }
            .check input { margin-top: .3rem; width: 1.1rem; height: 1.1rem; accent-color: var(--primary); }
            fieldset { border: 0; padding: 0; margin: 0 0 1rem; }
            .feelings { display: flex; flex-wrap: wrap; gap: .5rem; }
            .feeling-opt span { display: inline-block; padding: .45rem .9rem; border-radius: 999px; border: 1.5px solid var(--border); background: var(--surface); cursor: pointer; font-weight: 600; font-size: .95rem; }
            .feeling-opt input:checked + span { background: var(--primary-soft); border-color: var(--primary); color: var(--primary-dark); }
            .feeling-opt input:focus-visible + span { outline: 3px solid var(--primary); outline-offset: 2px; }
            .feeling-opt:hover span { border-color: var(--primary); }
            .form-actions { display: flex; gap: .6rem; flex-wrap: wrap; margin-top: 1rem; }

            .hero { text-align: center; padding: 3rem 0 2rem; }
            .eyebrow { display: inline-block; background: var(--primary-soft); color: var(--primary-dark); font-weight: 700; font-size: .85rem; padding: .3rem .8rem; border-radius: 999px; margin-bottom: 1rem; }
            .hero h1 { font-size: clamp(2.2rem, 7vw, 3.4rem); margin-bottom: 1rem; }
            .hero h1 em { font-style: normal; color: var(--primary); }
            .lead { font-size: 1.15rem; color: var(--muted); max-width: 34rem; margin: 0 auto 1.5rem; }
            .hero-cta { display: flex; gap: .75rem; justify-content: center; flex-wrap: wrap; }
            .grid3 { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-top: 2rem; }
            .grid3 .card { margin: 0; }
            .grid3 .icon { font-size: 1.8rem; display: block; margin-bottom: .4rem; }

            .filters { display: grid; grid-template-columns: 1fr; gap: .6rem; margin-bottom: 1rem; }
            @media (min-width: 640px) { .filters { grid-template-columns: 1.6fr 1fr 1fr auto auto; } }

            .post-head { display: flex; align-items: center; gap: .7rem; margin-bottom: .6rem; }
            .post-who { display: flex; flex-direction: column; line-height: 1.25; flex: 1; min-width: 0; }
            .post-who strong { overflow: hidden; text-overflow: ellipsis; }
            .avatar { width: 40px; height: 40px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; font-weight: 800; color: #fff; flex: none; }
            .avatar.sm { width: 32px; height: 32px; font-size: .85rem; }
            .a0 { background: #6c4df5; } .a1 { background: #0e9f93; } .a2 { background: #d6336c; } .a3 { background: #e8590c; }
            .a4 { background: #1c7ed6; } .a5 { background: #2f9e44; } .a6 { background: #ae3ec9; } .a7 { background: #d9480f; }
            .feeling-tag { background: var(--surface-2); border-radius: 999px; padding: .25rem .7rem; font-size: .85rem; font-weight: 700; white-space: nowrap; }
            .post-body { font-size: 1.05rem; overflow-wrap: anywhere; margin-bottom: .8rem; }
            .chip { display: inline-block; background: var(--primary-soft); color: var(--primary-dark); border-radius: 999px; padding: .15rem .7rem; font-size: .8rem; font-weight: 700; text-decoration: none; }
            .chip.warn { background: var(--danger-soft); color: var(--danger); }
            .post-meta { margin-bottom: .6rem; display: flex; gap: .4rem; flex-wrap: wrap; }
            .post-actions { display: flex; align-items: center; gap: .4rem; flex-wrap: wrap; border-top: 1px solid var(--border); padding-top: .7rem; }
            .react-btn { border: 1.5px solid var(--border); background: transparent; color: var(--text); border-radius: 999px; padding: .3rem .8rem; font: inherit; font-size: .9rem; font-weight: 600; cursor: pointer; }
            .react-btn:hover { background: var(--surface-2); }
            .react-btn.active { background: var(--primary-soft); border-color: var(--primary); color: var(--primary-dark); }
            .spacer { flex: 1; }
            .link-btn { background: none; border: 0; padding: .3rem .4rem; font: inherit; font-size: .9rem; font-weight: 600; color: var(--primary-dark); cursor: pointer; text-decoration: underline; }
            .link-btn.danger { color: var(--danger); }

            .comments { list-style: none; padding: 0; margin: 0 0 1rem; }
            .comment { display: flex; gap: .7rem; padding: .8rem 0; border-top: 1px solid var(--border); }
            .comment:first-child { border-top: 0; }
            .comment-body { flex: 1; min-width: 0; }
            .comment-body p { margin: .2rem 0 .2rem; overflow-wrap: anywhere; }
            .empty { text-align: center; padding: 2.5rem 1rem; color: var(--muted); }
            .empty .icon { font-size: 2.2rem; display: block; }

            .pager { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem; }

            .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: .8rem; margin-bottom: 1rem; }
            .stat { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: .9rem 1rem; box-shadow: var(--shadow); }
            .stat b { display: block; font-size: 1.8rem; line-height: 1.1; }
            .stat span { color: var(--muted); font-size: .85rem; }
            .bars { display: grid; grid-template-columns: auto 1fr auto; gap: .35rem .8rem; align-items: center; font-size: .92rem; }
            progress { width: 100%; height: .7rem; appearance: none; -webkit-appearance: none; border: 0; border-radius: 999px; background: var(--surface-2); overflow: hidden; }
            progress::-webkit-progress-bar { background: var(--surface-2); }
            progress::-webkit-progress-value { background: var(--primary); border-radius: 999px; }
            progress::-moz-progress-bar { background: var(--primary); border-radius: 999px; }
            .two-col { display: grid; gap: 1rem; grid-template-columns: 1fr; }
            @media (min-width: 760px) { .two-col { grid-template-columns: 1fr 1fr; } }

            .footer { text-align: center; color: var(--muted); font-size: .85rem; padding: 1.2rem 16px 2rem; border-top: 1px solid var(--border); }
            .footer p { max-width: 40rem; margin: 0 auto .3rem; }
            """;

    public static final String JS = """
            (function () {
              // Ask "are you sure?" before delete / remove buttons.
              document.addEventListener('submit', function (e) {
                var msg = e.target.getAttribute('data-confirm');
                if (msg && !window.confirm(msg)) { e.preventDefault(); }
              });

              // Live character counter under a text box: <textarea data-counter="id">.
              document.querySelectorAll('textarea[data-counter]').forEach(function (box) {
                var out = document.getElementById(box.getAttribute('data-counter'));
                if (!out) { return; }
                var max = box.getAttribute('maxlength');
                function update() { out.textContent = box.value.length + ' / ' + max; }
                box.addEventListener('input', update);
                update();
              });
            })();
            """;
}
