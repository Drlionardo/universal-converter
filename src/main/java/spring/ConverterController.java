package spring;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import spring.dto.InputDTO;
import spring.serivce.UniversalConverterService;
import spring.serivce.exceptions.UnableToConvertException;
import spring.serivce.exceptions.UnknownUnitException;

@RestController
public class ConverterController {
    private final UniversalConverterService universalConverterService;

    public ConverterController(UniversalConverterService universalConverterService) {
        this.universalConverterService = universalConverterService;
    }

    @PostMapping("/convert")
    public String convert(@RequestBody InputDTO fullInput) {
        String from = fullInput.getFrom();
        String to = fullInput.getTo();
        try {
            return universalConverterService.convert(from, to);
        } catch (UnableToConvertException e1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversion is not possible", e1);
        } catch (UnknownUnitException e2) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsupported unit in expression", e2);
        }
    }
}
