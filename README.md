# Laboratory 9 - Coroutines

```kotlin
/*
 * Suspending vs Blocking
 * 
 * Blocking (e.g. Thread.sleep()): Freezes the thread completely. No other task 
 * or coroutine assigned to that thread can run until the sleep finishes.
 * 
 * Suspending (e.g. delay()): Pauses the coroutine without blocking the thread. 
 * The thread is freed up to execute other coroutines in the meantime, and resumes 
 * this coroutine when the delay finishes.
 */
```

---

## Question 1. First coroutine

Output:
```
Order accepted
Coffee ready
```

Explanation: `launch` starts the coffee coroutine asynchronously in the background. The main execution doesn't wait for it and prints `"Order accepted"` immediately. The launched coroutine waits 1 second with `delay` before printing `"Coffee ready"`.

---

## Question 2. Three baristas

Output with `delay`:
```
Espresso start
Tea start
Latte start
Tea end
Latte end
Espresso end
```

Output with `Thread.sleep`:
```
Espresso start
Espresso end
Tea start
Tea end
Latte start
Latte end
```

Explanation: With `delay`, the thread suspends each coroutine and switches to run the others concurrently on the same thread, but with `Thread.sleep`, the thread is blocked entirely so each coroutine has to wait for the previous one to finish.

---

## Question 3. Chain of suspend functions

Output:
```
Step 1: User loaded
Step 2: Password checked
Step 3: Profile opened
```

Explanation: Calling suspend functions sequentially pauses at each step until it finishes, keeping the exact execution order without reordering any lines.

---

## Question 4. Calling suspend from normal code

Compile error:
```
Suspend function 'loadUser' should be called only from a coroutine or another suspend function
```

Two ways to fix it:
1. Make the caller a suspend function: `suspend fun login() { loadUser() }`
2. Call it inside `runBlocking`: `fun login() = runBlocking { loadUser() }`

UI Thread answer: You should never use `runBlocking` on the Android UI thread because it blocks the main thread, which freezes the app UI and causes Application Not Responding (ANR) errors.

---

## Question 5. Suspend is not a thread

Output:
```
Before call thread: main
Inside suspend func thread: main
```

Belief check: Disproved. Both lines run on `main`. Marking a function as `suspend` does not automatically move work to a background thread.

Compiler hidden parameter: `Continuation` (CPS - Continuation Passing Style).

---

## Question 6. Ten downloads, one thread

Output:
```
download 1 started on main
download 2 started on main
download 3 started on main
download 4 started on main
download 5 started on main
download 6 started on main
download 7 started on main
download 8 started on main
download 9 started on main
download 10 started on main
download 1 finished
download 2 finished
download 3 finished
download 4 finished
download 5 finished
download 6 finished
download 7 finished
download 8 finished
download 9 finished
download 10 finished
```

Concurrency vs Parallelism: This is concurrency. All 10 tasks make progress on a single thread by suspending during waits, taking turns on the thread rather than running at the exact same instant on multiple CPU cores.

---

## Question 7. Order of launch

Output:
```
1
4
2
5
3
```

Explanation: `"2"` does not print right after `"1"` because `launch` schedules the child coroutine on the event dispatcher, but the main code keeps running synchronously until it hits its own suspension point `delay(50)`. At that moment, the dispatcher gives control to the child coroutine.

---

## Question 8. Parent waits for child

Output:
```
parent body done
child done
report completed
```

Structured concurrency rule: A parent coroutine does not finish until all of its child coroutines have finished.

---

## Question 9. Screen closed, requests cancelled

Output:
```
Child 1 cleaned up
Child 2 cleaned up
end
```

Explanation: Cancelling the parent coroutine automatically cancels both child coroutines because children are bound to their parent in structured concurrency.

---

## Question 10. Profile ready

Output:
```
Settings loaded
Avatar loaded
Profile ready
end of main
Late task finished
```

Explanation: `coroutineScope` suspends and waits until both avatar and settings coroutines inside it complete before executing the line after it. On the other hand, `launch` fires off the late coroutine asynchronously and moves directly to print `"end of main"`.

---

## Question 11. Reusable counter

Output:
```
launched
Counter A: 1
Counter B: 1
Counter A: 2
Counter B: 2
Counter B: 3
```

Explanation: The `CoroutineScope` receiver is needed so `launch` can attach the counter coroutine to the caller's scope, following structured concurrency rules.

---

## Question 12. Returns at once or waits

Output:
```
Calling background helper...
Returned immediately from background helper
Calling suspend helper...
Hello sequential, Bob!
Hello sequential, Charlie!
Returned after suspend helper finished
Hello background, Alice!
```

Explanation: `greetBackground` is a normal function extending `CoroutineScope` that launches work in the background and returns right away. `greetAllSequential` is a `suspend` function using `coroutineScope`, so it suspends the caller and waits until all greetings finish.

---

## Question 13. Five builders

Output:
```
runBlocking returned: runBlocking result
launch returned: StandaloneCoroutine{Active}@...
async returned: DeferredCoroutine{Active}@...
coroutineScope returned: scope result
```

Cheat sheet:

| Builder | Return Type | Blocks Thread? | Waits for Children? |
|---|---|---|---|
| `launch` | `Job` | No | Yes (scope waits) |
| `async` | `Deferred<T>` | No | Yes (scope waits) |
| `runBlocking` | `T` (result) | Yes | Yes |
| `coroutineScope` | `T` (result) | No | Yes |
| `withContext` | `T` (result) | No | Yes |

---

## Question 14. Job or Deferred

Output:
```
Job cannot return value directly: StandaloneCoroutine{Completed}@...
Async result: 42
```

Explanation: A `Job` represents a running task and its state (active, completed, cancelled) but cannot return a value. A `Deferred<T>` is a type of `Job` that holds a result value which you get by calling `.await()`.

---

## Question 15. Three exports in a scope

Output:
```
Start
Export Y
Export Z
Export X
All done
```

Explanation: `coroutineScope` automatically waits for all three launched child coroutines (Y, Z, and X) to finish before letting the code continue to print `"All done"`.

---

## Extra 1. Delay or sleep: measure it

Output:
```
Delay total time: 304ms (~300ms)
Sleep total time: 909ms (~900ms)
```

Explanation: `delay` lets coroutines suspend concurrently on the single thread so their 300 ms delays run at the same time (~300 ms total). `Thread.sleep` blocks the thread completely so each coroutine has to wait 300 ms one after another (300 + 300 + 300 = ~900 ms total).
