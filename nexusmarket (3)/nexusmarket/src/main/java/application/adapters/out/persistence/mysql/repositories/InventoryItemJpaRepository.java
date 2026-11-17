package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InventoryItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemEntity, String> {

    Optional<InventoryItemEntity> findByVariant_IdentifierAndWarehouse_Identifier(String variantIdentifier,
                                                                                   String warehouseIdentifier);

    /**
     * Locks the stock rows of a variant until the end of the current transaction, so that two
     * buyers confirming orders at the same time cannot reserve the same units.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItemEntity i where i.variant.identifier = :variantIdentifier")
    List<InventoryItemEntity> findByVariantForUpdate(@Param("variantIdentifier") String variantIdentifier);

    List<InventoryItemEntity> findByWarehouse_Identifier(String warehouseIdentifier);
}
