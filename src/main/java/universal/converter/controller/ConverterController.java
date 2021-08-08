package universal.converter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import universal.converter.dto.InputDTO;
import universal.converter.exceptions.UnableToConvertException;
import universal.converter.exceptions.UnknownUnitException;
import universal.converter.serivce.ConverterService;

@RestController
public class ConverterController {
    private final ConverterService converterService;

    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/convert")
    public String convert(@RequestBody InputDTO fullInput) {
        String from = fullInput.getFrom();
        String to = fullInput.getTo();
        try {
            return converterService.convert(from, to);
        } catch (UnableToConvertException e1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversion is not possible", e1);
        } catch (UnknownUnitException e2) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsupported unit in expression", e2);
        }
    }
}
