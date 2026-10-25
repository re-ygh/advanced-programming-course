# Chess Crusader

A two-player strategy game written in Java Swing for an Advanced Programming course. It uses **custom movement and power rules** rather than standard chess rules. Players choose a 4-column or 8-column board, take turns moving and capturing pieces, and can chat during the game. Capturing the opposing castle ends the match.

## Run

Requires JDK 17 or newer. On Windows PowerShell, from this folder:

```powershell
.\run.ps1
```

Open a second PowerShell window and run the same command. Enter a name in each game window. Choose **Host** in the first window and **Join** in the second. The host then selects the board width.

For two computers on the same network, run the host normally and start the joining player with `./run.ps1 -HostAddress <host-ip>`. TCP ports **8080** and **1010** must be reachable. The two-computer setup has not been tested; the verified setup is two processes on one computer.

Run `./run.ps1 -BuildOnly` to compile without opening the game. The script writes generated files to `build/`; only `src/` is needed in version control.

## Verified behavior

Tested with JDK 21 on Windows on October 25, 2026:

- Both 4-column and 8-column boards opened in two game processes.
- Moves propagated in both directions and turns alternated.
- Invalid movement was rejected without consuming the turn.
- Capturing a piece removed it on both boards.
- Chat messages reached the other player.

The castle-capture ending and play across two computers have not yet been exercised end to end. There is no AI opponent, save game, or restart flow.

## فارسی

«Chess Crusader» یک بازی استراتژی دونفره با قوانین اختصاصی است و شطرنج استاندارد نیست. برنامه با جاوا و Swing نوشته شده و دو اندازهٔ صفحه، حرکت نوبتی، گرفتن مهره و گفت‌وگوی داخل بازی دارد. برای اجرا، دستور بالا را در دو پنجرهٔ PowerShell اجرا کنید؛ یکی میزبان شود، دیگری وصل شود، و سپس میزبان اندازهٔ صفحه را انتخاب کند.

این نسخه از روی پروژهٔ دانشگاهی بازبینی و باگ‌های اصلی اتصال و حرکت آن اصلاح شده است. وضعیت قابلیت‌های آزموده‌شده و محدودیت‌های فعلی در بخش‌های بالا آمده است.
