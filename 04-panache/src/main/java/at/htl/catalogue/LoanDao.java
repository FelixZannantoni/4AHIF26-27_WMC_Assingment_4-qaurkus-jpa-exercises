package at.htl.catalogue;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class LoanDao implements PanacheRepositoryBase<LoanEntity, LoanId> {
}
