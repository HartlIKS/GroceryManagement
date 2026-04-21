package de.iks.grocery_manager.server.model.mdi;

import de.iks.grocery_manager.server.model.HasUUID;
import de.iks.grocery_manager.server.model.mdi.handling.Parameter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Endpoint implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ExternalAPI api;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String baseUrl;

    @AttributeOverride(name = "header", column = @Column(name = "page_size_header"))
    @AttributeOverride(name = "queryParameter", column = @Column(name = "page_size_query_parameter"))
    private Parameter pageSize = new Parameter();
    @AttributeOverride(name = "header", column = @Column(name = "page_header"))
    @AttributeOverride(name = "queryParameter", column = @Column(name = "page_query_parameter"))
    private Parameter page = new Parameter();
    @AttributeOverride(name = "header", column = @Column(name = "item_count_header"))
    @AttributeOverride(name = "queryParameter", column = @Column(name = "item_count_query_parameter"))
    private Parameter itemCount = new Parameter();

    @Column(nullable = false)
    private String basePath;
}
