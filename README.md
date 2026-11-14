# IEEE754Extended

Program konsolowy w Javie, który zamienia liczbę dziesiętną na 80-bitowy zapis binarny w formacie extended precision (x87, standard IEEE 754). Wynik jest rozbity na znak, wykładnik i mantysę oraz podany w zapisie szesnastkowym (hex) z odstępami co 4 cyfry.

Java nie ma natywnego 80-bitowego typu zmiennoprzecinkowego, dlatego konwersja jest zrobiona ręcznie na klasach `BigDecimal` i `BigInteger`. Dzięki temu liczba nie jest wcześniej zaokrąglana przez `double`.

## Opis działania

1. Program wypisuje maksymalną dodatnią i ujemną liczbę, jaką da się zapisać, i prosi o podanie liczby dziesiętnej (kropka lub przecinek jako separator, dozwolona notacja naukowa, np. `1e-5`). Odczytywany jest tylko pierwszy wyraz wpisanego tekstu.
2. Zamienia przecinek na kropkę i zamienia tekst na `BigDecimal` (dokładna wartość, bez utraty precyzji). Jeśli tekst nie jest liczbą, wyświetla komunikat o błędzie.
3. Jeśli wpisano ujemne zero (`-0`, `-0.0`, `-0e0` itp.), wyświetla komunikat `NIE MA TAKIEJ LICZBY` i kończy działanie.
4. Ustala bit znaku na podstawie tego, czy wpisany tekst zaczyna się od `-`, a dalej liczy na wartości bezwzględnej.
5. Jeśli liczba jest zerem (`0`, `+0`, `0.0`), zwraca same zera (znak `0`, wykładnik i mantysa złożone z samych zer).
6. W pozostałych przypadkach przedstawia liczbę jako ułamek `licznik / mianownik` i wyznacza wykładnik `e`, taki że `2^e <= liczba < 2^(e+1)`.
7. Wylicza 64-bitową mantysę dzieleniem całkowitoliczbowym z resztą i zaokrągla ją do najbliższej parzystej (round-half-to-even). Jeśli po zaokrągleniu mantysa urośnie do 65 bitów, cofa ją o jeden bit i zwiększa wykładnik.
8. Dodaje bias 16383 do wykładnika i sprawdza, czy mieści się w zakresie liczb znormalizowanych (1 do 32766).
9. Skleja 80 bitów (`1 + 15 + 64`) w jedną liczbę `BigInteger` i zamienia ją na hex (20 cyfr, wielkie litery, spacja co 4 cyfry).
10. Wypisuje znak, wykładnik, mantysę, pełny zapis binarny oraz hex.

W przypadku błędnych danych lub liczby spoza zakresu program wyświetla komunikat o błędzie.

## Budowa 80 bitów

| Pole      | Bity | Opis                                                                 |
|-----------|------|----------------------------------------------------------------------|
| znak      | 1    | `0` = dodatnia, `1` = ujemna                                         |
| wykładnik | 15   | zapisany z przesunięciem (bias 16383)                                |
| mantysa   | 64   | pierwszy bit to **jawna** jedynka (inaczej niż w `float` i `double`) |

## Wartość liczby

```
(-1)^znak × (mantysa / 2^63) × 2^(wykładnik - 16383)
```

## Zakres

| Wartość                          | Zapis                               | Hex                        |
|----------------------------------|-------------------------------------|----------------------------|
| Maksymalna dodatnia              | ok. `1.18973149535723176502E+4932`  | `7FFE FFFF FFFF FFFF FFFF` |
| Maksymalna ujemna (co do modułu) | ok. `-1.18973149535723176502E+4932` | `FFFE FFFF FFFF FFFF FFFF` |
| Najmniejsza znormalizowana dodatnia | ok. `3.36210314311209350626E-4932` | `0001 8000 0000 0000 0000` |

## Wykorzystane elementy Javy

- klasa `Scanner`
- klasy `BigDecimal` i `BigInteger` (m.in. `unscaledValue()`, `scale()`, `signum()`, `shiftLeft()`, `shiftRight()`, `divideAndRemainder()`, `bitLength()`, `testBit()`)
- `BigInteger.toString(2)` i `toString(16)` do zapisu binarnego i szesnastkowego
- `Integer.toBinaryString()`
- `String.format()`, `replace()`, `replaceAll()` (wyrażenie regularne do spacji w hex) oraz `repeat()`
- obsługa wyjątku `NumberFormatException`

## Uruchomienie

```bash
javac IEEE754Extended.java
java IEEE754Extended
```

Wymagana jest Java 11 lub nowsza (metoda `String.repeat()`).

## Przykłady

| Wejście | Znak | Wykładnik         | Mantysa                                                            | Hex                        |
|---------|------|-------------------|--------------------------------------------------------------------|----------------------------|
| 5.75    | `0`  | `100000000000001` | `1011100000000000000000000000000000000000000000000000000000000000` | `4001 B800 0000 0000 0000` |
| 1       | `0`  | `011111111111111` | `1000000000000000000000000000000000000000000000000000000000000000` | `3FFF 8000 0000 0000 0000` |
| -0,1    | `1`  | `011111111111011` | `1100110011001100110011001100110011001100110011001100110011001101` | `BFFB CCCC CCCC CCCC CCCD` |
| 0       | `0`  | `000000000000000` | `0000000000000000000000000000000000000000000000000000000000000000` | `0000 0000 0000 0000 0000` |
| -0      | komunikat `NIE MA TAKIEJ LICZBY` | | | |
| abc     | komunikat o błędzie | | | |
| 1e5000  | komunikat o błędzie (poza zakresem) | | | |

## Uwagi

- Liczba `0.1` nie ma dokładnego zapisu binarnego, dlatego mantysa jest zaokrąglona (`...1001101`).
- `-0` jest poprawną wartością w IEEE 754, lecz w tym programie traktujemy je jako błąd matematyczny (ujemne zero nie istnieje).
- Program nie obsługuje liczb zdenormalizowanych (poniżej ok. `3.36210314311209350626E-4932`), nieskończoności ani `NaN`. Dla takich wartości wyświetla komunikat o błędzie.
- Liczba niezerowa o bardzo małym module (poniżej zakresu znormalizowanego) również kończy się komunikatem o błędzie, a nie zapisem zdenormalizowanym.
- Zero jest jedyną liczbą zapisywaną z wykładnikiem równym zero.

## Autor

Szymon Rybikowski
