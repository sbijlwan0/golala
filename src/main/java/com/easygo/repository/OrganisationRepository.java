package com.easygo.repository;

import java.util.List;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Organisation;

@Repository
public interface OrganisationRepository extends MongoRepository<Organisation, String> {

	List<Organisation> findAllByVendorId(@Param("vendorId") String vendorId);

	Page<Organisation> findAllByActivated(@Param("activated") boolean activated, Pageable pageable);

	Page<Organisation> findAllByActivatedIsTrueAndOpenIsTrueAndLocationNear(@Param("point") Point point, @Param("d") Distance d,
			Pageable pageable);

	List<Organisation> findAllByActivatedAndOpenIsTrueAndLocationNear(@Param("activated") boolean activated,
			@Param("point") Point point, @Param("d") Distance d);

	Page<Organisation> findAllByActivatedAndAddressLikeIgnoreCase(@Param("activated") boolean activated,
			@Param("address") String address, Pageable pageable);
}
