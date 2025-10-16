package org.fyp.emssep490be.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fyp.emssep490be.entities.ids.PloCloMappingId;

@Entity
@Table(name = "plo_clo_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PloCloMapping {

    @EmbeddedId
    private PloCloMappingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ploId")
    @JoinColumn(name = "plo_id", nullable = false)
    private Plo plo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cloId")
    @JoinColumn(name = "clo_id", nullable = false)
    private Clo clo;

    private String status;
}
