package CampusLab.ms_campuslab_catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceType type;

    @Column(length = 1000)
    private String description;

    private String location;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(nullable = false)
    private boolean active;

    protected Resource() {
        // JPA
    }

    public Resource(String name, ResourceType type, String description, String location,
                     int totalStock, int availableStock, boolean active) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.location = location;
        this.totalStock = totalStock;
        this.availableStock = availableStock;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Decrementa el cupo disponible en 1 al aprobarse una reserva.
     * No deja el stock negativo (regla simple, sin locking distribuido).
     */
    public void decrementAvailableStock() {
        if (availableStock > 0) {
            availableStock--;
        }
    }

    /**
     * Incrementa el cupo disponible en 1 al devolverse o cancelarse una reserva.
     * No supera el totalStock.
     */
    public void incrementAvailableStock() {
        if (availableStock < totalStock) {
            availableStock++;
        }
    }
}
