import universal.converter.serivce.ConverterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import universal.converter.exceptions.UnableToConvertException;
import universal.converter.exceptions.UnknownUnitException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
//TODO: Multithreading test, large dataset test, replace expected with actual;
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
            assertEquals(converterService.convert("км", "км"), "1 км = 1 км");
            assertEquals(converterService.convert("км", "м"), "1 км = 1000 м");
            assertEquals(converterService.convert("    км    ", "   м  "), "1 км = 1000 м");
            assertEquals(converterService.convert("км    ", "м  "), "1 км = 1000 м");
        }

        @Test
        void complexConvertTest() {
            assertEquals(converterService.convert("м/с", "км/час"), "1 м / с = 3.6 км / час");
            assertEquals(converterService.convert("м / с", "км / час"), "1 м / с = 3.6 км / час");
            assertEquals(converterService.convert("м /с", "км/ час"), "1 м / с = 3.6 км / час");
            assertEquals(converterService.convert("м/  с  ", "  км / час"), "1 м / с = 3.6 км / час");
        }

        @Test
        void nonLinearCovertTest() {
            assertEquals(converterService.convert("км*м*м/час*с*с", "см*км*км/мин*мин*мин"),
                    "1 км * м * м / час * с * с = 6 см * км * км / мин * мин * мин");
        }

        @Test
        void longLabelTest() {
            assertEquals(converterService.convert("м*км*м*мм/с*мин", "мм*км*м*м/мин*час"),
                    "1 м * км * м * мм / с * мин = 3600 мм * км * м * м / мин * час");
        }

        @Test
        void emptyStringConvertTest() {
            assertEquals(converterService.convert("", ""), "1 = 1");
            assertEquals(converterService.convert("1", ""), "1 = 1");
            assertEquals(converterService.convert("", "1"), "1 = 1");
            assertEquals(converterService.convert("1", "1"), "1 = 1");
            assertEquals(converterService.convert("", "км / м"), "1 = 0.001 км / м");
            assertEquals(converterService.convert("км / м", ""), "1 км / м = 1000");
        }

        @Test
        //Вывод 15 значащих цифр + округление вверх
        void accuracyTest() {
            //км,тест,1234567890123456789
            assertEquals(converterService.convert("км","тест"),"1 км = 1234567890123460000 тест");
            assertEquals(converterService.convert("мм","тест"),"1 мм = 1234567890123.46 тест");
            assertEquals(converterService.convert("км/км*км*км*км","тест/мм*мм*мм*мм"),
                    "1 км / км * км * км * км = 0.00000123456789012346 тест / мм * мм * мм * мм");

            //км,тест2,1234567890987654321
            assertEquals(converterService.convert("км","тест2"),"1 км = 1234567890987650000 тест2");
            assertEquals(converterService.convert("мм","тест2"),"1 мм = 1234567890987.65 тест2");
            assertEquals(converterService.convert("км/км*км*км*км","тест2/мм*мм*мм*мм"),
                    "1 км / км * км * км * км = 0.00000123456789098765 тест2 / мм * мм * мм * мм");


        }
    }

    @Nested
    class ExceptionsTests {
        @Test
        void UnknownUnitExceptionTest() {
            assertThrows((UnknownUnitException.class), () -> converterService.convert("unknownValue","км"));
            assertThrows((UnknownUnitException.class), () -> converterService.convert("км","unknownValue"));
            assertThrows((UnknownUnitException.class), () -> converterService.convert("unknownValue","anotherUnknownValue"));
        }

        @Test
        void UnableToConvertExceptionTest() {
            assertThrows((UnableToConvertException.class), () -> converterService.convert("м/с","км"));
            assertThrows((UnableToConvertException.class), () -> converterService.convert("","км"));
        }
    }
}