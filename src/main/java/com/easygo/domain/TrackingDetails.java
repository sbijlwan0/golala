package com.easygo.domain;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

@org.springframework.data.mongodb.core.mapping.Document(collection="tracking_Details")
public class TrackingDetails {

    @Id
    private String id;
    
    private String driverId;
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint liveLocation;
    
    
    

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDriverId() {
		return driverId;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}

	public GeoJsonPoint getLiveLocation() {
		return liveLocation;
	}

	public void setLiveLocation(GeoJsonPoint liveLocation) {
		this.liveLocation = liveLocation;
	}

    
    
    
    
}
