(ns company-back.play-geo
  (:require
   [geo.geohash :as geohash]
   [geo.io :as gio]
   [geo.spatial :as spatial]))

; Find the earth's radius at the pole, in meters
(spatial/earth-radius spatial/south-pole)
6356752.3

; Or at 45 degrees north
(spatial/earth-radius (spatial/geohash-point 45 0))
6378137.0

; Distance between London Heathrow and LAX
(def lhr (spatial/spatial4j-point 51.477500 -0.461388))
(def lax (spatial/spatial4j-point 33.942495 -118.408067))
(/ (spatial/distance lhr lax) 1000)
8780.16854531993 ; kilometers

; LHR falls within a 50km radius of downtown london
(def london (spatial/spatial4j-point 51.5072 0.1275))
(spatial/intersects? lhr (spatial/circle london 50000))
true

; But it's not in the downtown area
(spatial/intersects? lhr (spatial/circle london 10000))
false

; At London, how many bits of geohash do we need for 100m features?
(geohash/shape->precision (spatial/circle london 50))
35

; Let's get the 35-bit geohash containing London's center:
(def h (geohash/geohash london 35))
h

; How tall/fat is this geohash, through the middle?
(geohash/geohash-midline-dimensions h)
[152.7895756415971 95.34671939564083]

; As a base32-encoded string, this hash is:
(geohash/string h)
"u10j4bs"

; We can drop characters off a geohash to get strict supersets of that hash.
(spatial/intersects? (geohash/geohash "u10j4") london)
true

; And we can show it's a strict superset by comparing the regions:
(spatial/relate (geohash/geohash "u10j4bs") (geohash/geohash "u10j4"))
:within
(spatial/relate (geohash/geohash "u10j4") (geohash/geohash "u10j4bs"))
:contains

; Versus, say, two adjacent hashes, which intersect along their edge
(spatial/relate h (geohash/northern-neighbor h))
:intersects

; But two distant geohashes do *not* intersect
(-> (iterate geohash/northern-neighbor h) (nth 5) (spatial/relate h))
:disjoint

; Find all 30-bit geohashes covering the 1km region around LHR
(map geohash/string (geohash/geohashes-intersecting (spatial/circle lhr 1000) 30))

; Or more directly
(map geohash/string (geohash/geohashes-near lhr 1000 30))

; Reading JTS Geometries to/from common geo formats
(gio/read-wkt "POLYGON ((-70 30, -70 30, -70 30, -70 30, -70 30))")

(gio/to-wkt (gio/read-wkt "POLYGON ((-70 30, -70 31, -71 31, -71 30, -70 30))"))
"POLYGON ((-70 30, -70 31, -71 31, -71 30, -70 30))"

(gio/read-geojson "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]},\"properties\":{\"name\":\"null island\"}}")

(->> "{\"type\":\"Polygon\",\"coordinates\":[[[-70.0,30.0],[-70.0,31.0],[-71.0,31.0],[-71.0,30.0],[-70.0,30.0]]]}"
     gio/read-geojson
     (map :geometry)
     (map gio/to-geojson))

(gio/to-wkb (gio/read-wkt "POLYGON ((-70 30, -70 31, -71 31, -71 30, -70 30))"))

(gio/read-wkb *1)
