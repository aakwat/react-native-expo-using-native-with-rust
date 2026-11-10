# CLAUDE.md

Rules for agents working in this repo. If a rule needs a paragraph to justify
itself, it is probably wrong.

## Working agreement

- Verify before claiming. Read the file, run the command, quote the output.
  Never report something as working that you have not seen work.
- Do not run builds, test suites or migrations unprompted. Name the command and
  let me run it.
- State the blast radius before editing: which files, which callers.
- Finish the task you were given. If you find a second problem, name it; do not
  silently widen scope.
- Ambiguity that changes the work is worth one question. Ambiguity that does not
  is worth a default and a sentence saying which you picked.
- Do not trust existing code or comments. They were right once.

## Comments

- One line. If it needs three, fix the code instead.
- Explain why, never what. `// increment i` is noise.
- One comment, one reason. Do not staple two unrelated facts together.
- Comment the non-obvious: a workaround, an ordering requirement, a constraint
  that is not visible locally, a deliberate omission.
- No commented-out code. Git remembers it.
- A stale comment is worse than none. Change it with the code or delete it.

## Commits

- One-line subject, imperative, no trailing period.
- No body unless the change is genuinely non-obvious. No trailers, no
  Co-Authored-By, no emoji.
- One logical change per commit. Use `git commit -- <path>` rather than sweeping
  in unrelated files.
- Never commit working-tree changes you did not make.

## Structure

- Single responsibility at every level: function, module, layer.
- No single large file. Split a file when it starts doing two things, not after
  it does four.
- Layer by layer. Dependencies point inward: the core knows nothing about
  transport, storage or frameworks.
- Module by module. A module exposes a small surface and hides the rest.
- Before adding an abstraction, say what breaks without it. One implementation
  needs no interface; a value that never changes needs no config.
- Deleting code is a valid change, and usually the better one.
- Shortest correct change in the right place. A small diff in the wrong place is
  a second bug.
- Re-think before you build: is this worth its own file, layer, or dependency?
  Most of the time the answer is no.

## Kotlin / Android

### Style

- `ktlint` formats and lints; the build runs it, not you.
- No `!!`. It is a crash with a delay. Use `?.`, `requireNotNull(x) { "why" }`,
  or restructure so nullability does not reach that point.
- Prefer `val`. `var` needs a reason.
- Sealed interfaces for UI state and for results. `when` over a sealed type is
  exhaustive, so adding a case becomes a compile error rather than a bug.
- Data classes for state; they give you equality, which Compose relies on to
  skip recomposition.

### Coroutines

- Suspend functions are main-safe: the function moves to its own dispatcher,
  the caller never has to know.
- Structured concurrency only. `viewModelScope` / `lifecycleScope`, never
  `GlobalScope`.
- `Flow` for streams, `StateFlow` for state the UI observes. Collect with
  `repeatOnLifecycle`, otherwise you keep collecting behind a dead screen.
- Pass a `CoroutineDispatcher` in rather than hardcoding `Dispatchers.IO`, so
  tests can substitute one.

### Layering

```
ui/      Compose; stateless composables, state hoisted to the ViewModel
domain/  use cases and entities; pure Kotlin, no Android imports
data/    repositories, DAOs, network; the only layer that knows Retrofit or Room
```

The test for `domain/` is that it compiles as a plain JVM module. If it needs
`android.*`, the dependency is pointing the wrong way.

- The ViewModel exposes state and takes events. It does not hold a `Context`,
  a `View`, or a navigation controller.
- Repositories return domain types, never DTOs or Room entities.

### Compose

- Composables are side-effect free. Effects belong in `LaunchedEffect`,
  `DisposableEffect`, or the ViewModel.
- Hoist state. A composable that owns the state it renders cannot be previewed
  or reused.
- Stable parameters, or recomposition will not skip when it should.
