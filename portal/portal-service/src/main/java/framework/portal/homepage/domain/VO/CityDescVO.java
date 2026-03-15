package framework.portal.homepage.domain.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityDescVO implements Serializable {
    private Long id;
    private String name;
    private String fullName;
}
