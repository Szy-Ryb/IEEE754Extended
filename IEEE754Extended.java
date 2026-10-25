import java.math.BigDecimal; // klasa do dokładnych liczb dziesiętnych (bez utraty precyzji)
import java.math.BigInteger; // klasa do bardzo dużych liczb całkowitych
import java.util.Scanner;

public class IEEE754Extended { // główna klasa programu w Javie

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in); // utworzenie obiektu scanner do inputu z klawiatury

        System.out.println("Copyright Szymon Rybikowski"); // copyright

        System.out.println("Maksymalna liczba dodatnia: 1.18973149535723176502E+4932  (hex: 7FFE FFFF FFFF FFFF FFFF)"); // największa dodatnia liczba znormalizowana
        System.out.println("Maksymalna liczba ujemna:  -1.18973149535723176502E+4932  (hex: FFFE FFFF FFFF FFFF FFFF)"); // największa co do modułu liczba ujemna

        System.out.print("Podaj liczbę dziesiętną (np. 5.75, -0.1, 1e-5): "); // prośba o wpisanie liczby

        String wejscie = scanner.next().replace(',', '.'); // pobranie liczby jako tekst, przecinek zamieniony na kropkę

        try {
            BigDecimal liczba = new BigDecimal(wejscie); // zamiana tekstu na dokładną liczbę dziesiętną

            String znak = wejscie.startsWith("-") ? "1" : "0"; // bit znaku: 1 dla liczby ujemnej, 0 dla dodatniej

            liczba = liczba.abs(); // dalej liczymy na wartości bezwzględnej

            if (liczba.signum() == 0) { // sprawdzenie czy liczba to zero

                String wykladnikZero = "0".repeat(15); // wykładnik zero to same zera
                String mantysaZero = "0".repeat(64); // mantysa zero to same zera
                BigInteger calosc = new BigInteger(znak + wykladnikZero + mantysaZero, 2); // sklejenie 80 bitów w jedną liczbę
                String hex = String.format("%20s", calosc.toString(16).toUpperCase()).replace(' ', '0'); // hex na 20 cyfr, wielkie litery
                String hexZeSpacjami = hex.replaceAll("(.{4})(?!$)", "$1 "); // spacja co 4 cyfry

                System.out.println("Znak:      " + znak); // zero ma sam znak
                System.out.println("Wykładnik: " + wykladnikZero); // wyświetlenie wykładnika
                System.out.println("Mantysa:   " + mantysaZero); // wyświetlenie mantysy
                System.out.println("Binarnie:  " + znak + " " + wykladnikZero + " " + mantysaZero); // wyświetlenie całości binarnie
                System.out.println("Hex:       " + hexZeSpacjami); // wyświetlenie całości szesnastkowo

            } else {

                BigInteger licznik = liczba.unscaledValue(); // liczba = licznik / mianownik
                BigInteger mianownik = BigInteger.ONE; // domyślny mianownik to 1

                if (liczba.scale() > 0) { // gdy liczba ma część ułamkową
                    mianownik = BigInteger.TEN.pow(liczba.scale()); // mianownik to potęga 10
                } else { // gdy liczba nie ma części ułamkowej (np. 1e5)
                    licznik = licznik.multiply(BigInteger.TEN.pow(-liczba.scale())); // mnożymy licznik przez potęgę 10
                }

                int wykladnik = licznik.bitLength() - mianownik.bitLength(); // przybliżony wykładnik e, gdzie 2^e <= liczba < 2^(e+1)

                BigInteger lewa = licznik; // lewa strona porównania
                BigInteger prawa = mianownik; // prawa strona porównania

                if (wykladnik >= 0) { // gdy wykładnik nieujemny
                    prawa = mianownik.shiftLeft(wykladnik); // mianownik razy 2^e
                } else { // gdy wykładnik ujemny
                    lewa = licznik.shiftLeft(-wykladnik); // licznik razy 2^(-e)
                }

                if (lewa.compareTo(prawa) < 0) { // jeśli przybliżenie było za duże
                    wykladnik--; // poprawka wykładnika o 1
                }

                int przesuniecie = 63 - wykladnik; // mantysa ma mieć 64 bity, więc przesuwamy o 63 - e
                BigInteger n = licznik; // kopia licznika do dzielenia
                BigInteger d = mianownik; // kopia mianownika do dzielenia

                if (przesuniecie >= 0) { // gdy trzeba przesunąć w lewo
                    n = n.shiftLeft(przesuniecie); // licznik razy 2^przesuniecie
                } else { // gdy trzeba przesunąć w prawo
                    d = d.shiftLeft(-przesuniecie); // mianownik razy 2^(-przesuniecie)
                }

                BigInteger[] wynikDzielenia = n.divideAndRemainder(d); // dzielenie z resztą
                BigInteger mantysa = wynikDzielenia[0]; // część całkowita to mantysa
                int porownanie = wynikDzielenia[1].shiftLeft(1).compareTo(d); // porównanie reszty z połową mianownika

                if (porownanie > 0 || (porownanie == 0 && mantysa.testBit(0))) { // zaokrąglanie do najbliższej parzystej
                    mantysa = mantysa.add(BigInteger.ONE); // zaokrąglenie w górę
                }

                if (mantysa.bitLength() == 65) { // sprawdzenie czy po zaokrągleniu mantysa urosła do 65 bitów
                    mantysa = mantysa.shiftRight(1); // cofnięcie mantysy do 64 bitów
                    wykladnik++; // zwiększenie wykładnika o 1
                }

                int wykladnikPrzesuniety = wykladnik + 16383; // dodanie biasu 16383 do wykładnika

                if (wykladnikPrzesuniety >= 1 && wykladnikPrzesuniety <= 32766) { // sprawdzenie czy liczba mieści się w zakresie

                    String wykladnikBinarny = String.format("%15s", Integer.toBinaryString(wykladnikPrzesuniety)).replace(' ', '0'); // wykładnik na 15 bitów

                    String mantysaBinarna = mantysa.toString(2); // mantysa binarnie (64 bity, pierwszy to jawna jedynka)

                    BigInteger calosc = new BigInteger(znak + wykladnikBinarny + mantysaBinarna, 2); // sklejenie 80 bitów w jedną liczbę

                    String hex = String.format("%20s", calosc.toString(16).toUpperCase()).replace(' ', '0'); // hex na 20 cyfr, wielkie litery

                    String hexZeSpacjami = hex.replaceAll("(.)(?!$)", "$1   "); // trzy spacje po każdej cyfrze (oprócz ostatniej)

                    System.out.println("Znak:      " + znak); // wyświetlenie bitu znaku
                    System.out.println("Wykładnik: " + wykladnikBinarny); // wyświetlenie wykładnika
                    System.out.println("Mantysa:   " + mantysaBinarna); // wyświetlenie mantysy
                    System.out.println("Binarnie:  " + znak + " " + wykladnikBinarny + " " + mantysaBinarna); // wyświetlenie całości binarnie
                    System.out.println("Hex:       " + hexZeSpacjami); // wyświetlenie całości szesnastkowo

                } else {
                    System.out.println("Błąd!! Liczba jest poza zakresem extended precision!"); // błąd gdy liczba wykracza poza zakres
                }
            }

        } catch (NumberFormatException e) { // wyjątek gdy input to inne znaki niż liczby
            System.out.println("Błąd!! To nie jest poprawna liczba!");
        }

        scanner.close(); // zamknięcie skanera w celu zwolnienia zasobów systemowych
    }
} // Copyright Szymon Rybikowski
