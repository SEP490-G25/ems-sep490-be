package org.fyp.emssep490be.entities.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PloCloMappingId implements Serializable {

    @Column(name = "plo_id")
    private Long ploId;

    @Column(name = "clo_id")
    private Long cloId;
}
