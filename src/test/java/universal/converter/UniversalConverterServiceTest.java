package universal.converter;

import universal.converter.serivce.ConverterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import universal.converter.exceptions.UnableToConvertException;
import universal.converter.exceptions.UnknownUnitException;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.*;

class UniversalConverterServiceTest {
    final static String testFilePath = "src/test/resources/INPUT.scv";
    static ConverterService converterService;

    @BeforeEach
    void setUp() {
        converterService = new ConverterService(testFilePath);
    }

    @Nested
    class ConvertTests {
        @Test
        void simpleConvertTest() {
            assertEquals("1 км = 1 км", converterService.convert("км", "км"));
            assertEquals("1 км = 1000 м", converterService.convert("км", "м") );
        }

        @Test
        void complexConvertTest() {
            assertEquals( "1 м / с = 3.6 км / час", converterService.convert("м / с", "км / час"));
        }

        @Test
        void nonLinearCovertTest() {
            assertEquals("1 км * м * м / час * с * с = 6 см * км * км / мин * мин * мин",
                    converterService.convert("км * м * м / час * с * с", "см * км * км / мин * мин * мин"));
        }

        @Test
        void longExpressionTest() {
            String from = "м *".repeat(99999) +"м";
            String to = "км *".repeat(99999) +"км";
            assertAll(
                    () -> assertEquals(new BigDecimal("1E-300000"), converterService.getConvertRatio(from, to)),
                    () -> assertEquals(new BigDecimal("1E+300000"), converterService.getConvertRatio(to, from))
            );
            assertEquals("1 м * км * м * мм / с * мин = 3600 мм * км * м * м / мин * час",
                    converterService.convert("м*км*м*мм/с*мин", "мм*км*м*м/мин*час"));
        }

        @Test
        void emptyStringConvertTest() {
            assertAll(
                    () -> assertEquals("1 = 1", converterService.convert("", "")),
                    () -> assertEquals("1 = 1", converterService.convert("1", "")),
                    () -> assertEquals("1 = 1", converterService.convert("", "1")),
                    () -> assertEquals("1 = 1", converterService.convert("1", "1")),
                    () -> assertEquals("1 = 0.001 км / м", converterService.convert("", "км / м")),
                    () -> assertEquals( "1 км / м = 1000", converterService.convert("км / м", ""))
            );
        }
    }

    //Вывод 15 значащих цифр + округление
    @Nested
    class accuracyTests {
        @Test
        void roundUpTest() {
            //км,тест,1234567890123456789
            assertAll(
                    () -> assertEquals( "1 км = 1234567890123460000 тест", converterService.convert("км", "тест")),
                    () -> assertEquals( "1 мм = 1234567890123.46 тест", converterService.convert("мм", "тест")),
                    () -> assertEquals("1 км / км * км * км * км = 0.00000123456789012346 тест / мм * мм * мм * мм",
                    converterService.convert("км/км*км*км*км", "тест/мм*мм*мм*мм"))
            );
        }

        @Test
        void roundDownTest(){
            //км,тест2,1234567890987654321
            assertAll(
                    () -> assertEquals("1 км = 1234567890987650000 тест2", converterService.convert("км","тест2")),
                    () -> assertEquals("1 мм = 1234567890987.65 тест2", converterService.convert("мм","тест2")),
                    () -> assertEquals("1 км / км * км * км * км = 0.00000123456789098765 тест2 / мм * мм * мм * мм",
                            converterService.convert("км/км*км*км*км","тест2/мм*мм*мм*мм"))
            );
        }
    }
    @Nested
    class ExceptionsTests {
        @Test
        void UnknownUnitExceptionTest() {
            assertAll(
                    () -> assertThrows((UnknownUnitException.class), () -> converterService.convert("unknownValue","км")),
                    () -> assertThrows((UnknownUnitException.class), () -> converterService.convert("км","unknownValue")),
                    () -> assertThrows((UnknownUnitException.class), () -> converterService.convert("unknownValue","anotherUnknownValue"))
            );
        }

        @Test
        void UnableToConvertExceptionTest() {
            assertAll(
                    () -> assertThrows((UnableToConvertException.class), () -> converterService.convert("м/с","км")),
                    () -> assertThrows((UnableToConvertException.class), () -> converterService.convert("","км")),
                    () -> assertThrows((UnknownUnitException.class), () -> converterService.convert("unknownValue","anotherUnknownValue")),
                    () -> assertThrows((UnknownUnitException.class), () -> converterService.convert("unknownValue","м /с "))
            );
        }
    }
}