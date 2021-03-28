package spring.serivce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import spring.serivce.exceptions.UnableToConvertException;
import spring.serivce.exceptions.UnknownUnitException;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;

/**
 * Конвертирует математические выражения используя {@code WeightedGraph}
 * для хранения правил конвертации.
 *
 * @author Danila Iagupets
 */
@Service
public class UniversalConverterService {
    private static final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);
    private final WeightedGraph graph;

    private enum Mode {
        NUMERATOR,
        DENOMINATOR
    }

    /**
     * @param applicationArguments используется для получения пути файла с правилами из
     *                             command-line аргументов ожидая, filePath первым аргументом.
     */
    @Autowired
    public UniversalConverterService(ApplicationArguments applicationArguments) {
        String filePath = applicationArguments.getSourceArgs()[0];
        this.graph = new WeightedGraph(new File(filePath));
    }

    public UniversalConverterService(String filePath) {
        this.graph = new WeightedGraph(new File(filePath));
    }

    /**
     * Преобразует математическое выражение, удаляя лишние проблемы, оставляя
     * только единицы измерения, разделенные между собой " * ".
     *
     * @param s математическое выражение имеющее вид дроби или полинома
     * @return массив преобразованных строк, где 0 элемент - числитель дроби, 1 - её знаменатель
     */
    private String[] splitStringInFraction(String s) {
        s = s.replaceAll("\\s", "");
        String numerator;
        String denominator;
        boolean hasDivision = false; //Наличие знака "/" в выражение
        int divisionIndex = -1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '/') {
                hasDivision = true;
                divisionIndex = i;
                break;
            }
        }
        if (hasDivision) {
            numerator = s.substring(0, divisionIndex);
            if (numerator.equals("1")) {
                numerator = "";
            }
            denominator = s.substring(divisionIndex + 1);
        } else {
            numerator = s;
            if (numerator.equals("1")) {
                numerator = "";
            }
            denominator = "";
        }
        numerator = numerator.replaceAll("\\*", " * ");
        denominator = denominator.replaceAll("\\*", " * ");
        return new String[]{numerator, denominator};
    }

    /**
     * Конвертирует выражение и округляет полученное значение до 15 значащих цифр
     * Выражения записываются с использованием обозначений для единиц измерений,
     * заданных в правилах конвертации, используя только знаки умножения * и деления /.
     * При этом знак деления может использоваться не более одного раза
     * и служит для записи обыкновенной дроби. Допускается использование "1" в числителе,
     * если в нем отсутствуют иные единицы измерения. Прим: "1 / м"
     *
     * @param from Исходная выражение
     * @param to   Конечное выражение
     * @return Форматированная строка вида "1 м / с = 3.6 км / час" для from="м/с" to="км"
     */
    public String convert(String from, String to) {
        String[] fromUnits = splitStringInFraction(from);
        String[] toUnits = splitStringInFraction(to);
        String formattedRatio = calculateRatio(from, to).
                round(new MathContext(15, RoundingMode.HALF_UP))
                .stripTrailingZeros().toPlainString();

        StringBuilder output = new StringBuilder();
        output.append(1);
        if (!fromUnits[0].isEmpty()) {
            output.append(" ").append(fromUnits[0]);
        }
        if (!fromUnits[1].isEmpty()) {
            output.append(" / ").append(fromUnits[1]);
        }

        output.append(" = ").append(formattedRatio);

        if (!toUnits[0].isEmpty()) {
            output.append(" ").append(toUnits[0]);
        }
        if (!toUnits[1].isEmpty()) {
            output.append(" / ").append(toUnits[1]);
        }
        return output.toString();
    }


    /**
     * Рассчитывает коэффициент преобразования.
     *
     * @param from Исходная выражение для конвертации
     * @param to   Конечное выражение для конвертации
     * @return Итоговый коэффициент преобразование, означающий кол-во "to" в 1 единице "from"
     * @throws UnableToConvertException если конвертация невозможна
     * @implNote Каждое выражение представляется в виде отдельных групп единиц измерения,
     * связанных между собой.
     * Единицы измерения в пределах одной группы конвертируются до первого элемента
     * данной группы. Затем результаты для всех групп перемножаются и рассчитывается
     * отношения между исходным и конечным выражением
     */
    private BigDecimal calculateRatio(String from, String to) {
        HashMap<Integer, BigDecimal> inputGroupRatio = new HashMap<>();
        HashMap<Integer, BigDecimal> outputGroupRatio = new HashMap<>();
        HashMap<Integer, Integer> inputUnitsPowers = new HashMap<>();
        HashMap<Integer, Integer> outputUnitsPowers = new HashMap<>();
        String[] fromFormattedFraction = splitStringInFraction(from);
        String[] toFormattedFraction = splitStringInFraction(to);

        unitsConverter(inputUnitsPowers, inputGroupRatio, fromFormattedFraction, Mode.NUMERATOR);
        unitsConverter(inputUnitsPowers, inputGroupRatio, fromFormattedFraction, Mode.DENOMINATOR);
        unitsConverter(outputUnitsPowers, outputGroupRatio, toFormattedFraction, Mode.NUMERATOR);
        unitsConverter(outputUnitsPowers, outputGroupRatio, toFormattedFraction, Mode.DENOMINATOR);

        //Проверка на возможность конвертации при совпадение степеней для каждой из групп единиц измерения
        if (inputUnitsPowers.equals(outputUnitsPowers)) {
            BigDecimal inputProduct = BigDecimal.ONE;
            BigDecimal outputProduct = BigDecimal.ONE;
            for (BigDecimal ratio : inputGroupRatio.values()) {
                inputProduct = inputProduct.multiply(ratio);
            }
            for (BigDecimal ratio : outputGroupRatio.values()) {
                outputProduct = outputProduct.multiply(ratio);
            }
            return inputProduct.divide(outputProduct, MATH_CONTEXT);
        } else {
            throw new UnableToConvertException();
        }
    }

    /**
     * @param componentPower    Хранит значения степеней каждой единицы измерения, кроме нулевых.
     *                          Используется только для дальнейшей проверки
     *                          на возможность конвертации сложных выражений
     * @param componentRatio    Хранит значения полученные при конвертации до нулевого элемента
     *                          в компоненте связности для каждой единицы измерения
     * @param formattedFraction Массив строк,содержащий математическое выражение для конвертации
     *                          в виде дроби,где 0 элемент - числитель, 1 - знаменатель.
     *                          Единицы измерения разделены " * "
     * @param mode              Режим для конвертации NUMERATOR/DENOMINATOR
     *                          для числителя и знаменателя соответственно
     * @throws UnknownUnitException если найдена единица измерения
     *                              для которой не существует правила конвертации
     */
    private void unitsConverter(HashMap<Integer, Integer> componentPower, HashMap<Integer, BigDecimal> componentRatio,
                                String[] formattedFraction, Mode mode) {
        String unitsScope = (mode == Mode.NUMERATOR) ? (formattedFraction[0]) : (formattedFraction[1]);
        for (String unit : unitsScope.split(" \\* ")) {
            if (unit.length() != 0) {
                WeightedGraph.Vertex currentVertex = graph.getVertexByLabel(unit);
                if (currentVertex == null) {
                    throw new UnknownUnitException();
                }
                int componentId = currentVertex.getComponentIndex();
                componentPower.putIfAbsent(componentId, 0);
                componentRatio.putIfAbsent(componentId, BigDecimal.ONE);
                WeightedGraph.Vertex mainVertex = graph.getComponents().get(componentId).get(0);
                BigDecimal ratio = graph.calcRatioBetweenVertexes(currentVertex, mainVertex);
                int currentPower = componentPower.getOrDefault(componentId, 0);
                BigDecimal currentRatio = componentRatio.get(componentId);
                if (mode == Mode.NUMERATOR) {
                    componentPower.put(componentId, currentPower + 1);
                    componentRatio.put(componentId, currentRatio.multiply(ratio));
                } else if (mode == Mode.DENOMINATOR) {
                    componentPower.put(componentId, currentPower - 1);
                    componentRatio.put(componentId, currentRatio.divide(ratio, MATH_CONTEXT));
                }
                //Удаление нулевых степеней, поскольку безразмерные величины не влияют на возможность конвертации
                if (componentPower.get(componentId) == 0) {
                    componentPower.remove(componentId);
                }
            }
        }
    }
}
