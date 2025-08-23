package com.example.markerclient.domain.map

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData

private val exampleGeoJsonData = """
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [-75.6972, 45.4215]
      },
      "properties": {
        "title": "Ottawa Downtown",
        "description": "City center"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [-75.7000, 45.4200]
      },
      "properties": {
        "title": "Restaurant",
        "description": "Great local food"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [-75.6950, 45.4230]
      },
      "properties": {
        "title": "Hospital",
        "description": "Emergency services"
      }
    }
  ]
}
""".trimIndent()

fun GetExampleGeoJsonData() : GeoJSONData {
    return GeoJSONData(exampleGeoJsonData)
}

fun GetExampleFeatureCollection() : FeatureCollection {
    return FeatureCollection.fromJson(exampleGeoJsonData)
}

// Get list of features or empty list if null
fun GetExampleListOfFeatures() : List<Feature> {
    return FeatureCollection.fromJson(exampleGeoJsonData).features() ?: listOf()
}