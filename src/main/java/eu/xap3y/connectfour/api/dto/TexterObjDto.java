package eu.xap3y.connectfour.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.io.File;


@Getter
@Setter
public class TexterObjDto {

    private String prefix;
    private boolean debug;

    @Nullable
    private File debugFile;

    public TexterObjDto() {
        this.prefix = "";
        this.debug = false;
        this.debugFile = null;
    }

    public TexterObjDto(String prefix, boolean debug, @Nullable File debugFile) {
        this.prefix = prefix;
        this.debug = debug;
        this.debugFile = debugFile;
    }

    public TexterObjDto(String prefix) {
        this.prefix = prefix;
        this.debug = false;
        this.debugFile = null;
    }
}
