package framework.portal.homepage.domain.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class DictsVO implements Serializable {
    private Long id;
    private String key;
    private String name;
}
