(in-ns 'play-clj.core)

; renderers

(defn load-tiled-map
  [{:keys [file]}]
  (assert (string? file))
  (.load (TmxMapLoader.) file))

(defn unit-scale
  [{:keys [tile-size]}]
  (assert (number? tile-size))
  (float (/ 1 tile-size)))

(defn render!
  [{:keys [renderer ^Camera camera]}]
  (assert renderer)
  (cond
    (isa? (type renderer) BatchTiledMapRenderer)
    (doto ^BatchTiledMapRenderer renderer
      (.setView camera)
      .render)
    (isa? (type renderer) Stage)
    (.draw ^Stage renderer)))

(defn tiled-map-layer
  [{:keys [^BatchTiledMapRenderer renderer]} ^String layer]
  (assert renderer)
  (-> renderer .getMap .getLayers (.get layer)))

(defn tiled-map-cell
  [{:keys [^BatchTiledMapRenderer renderer] :as screen} layer x y]
  (assert renderer)
  (-> (if (or (string? layer) (number? layer))
        ^TiledMapTileLayer (tiled-map-layer screen layer)
        ^TiledMapTileLayer layer)
      (.getCell x y)))

(defmulti renderer :type :default nil)

(defmethod renderer nil [opts])

(defmethod renderer :orthogonal-tiled-map [opts]
  (OrthogonalTiledMapRenderer. ^TiledMap (load-tiled-map opts)
                               ^double (unit-scale opts)))

(defmethod renderer :isometric-tiled-map [opts]
  (IsometricTiledMapRenderer. ^TiledMap (load-tiled-map opts)
                              ^double (unit-scale opts)))

(defmethod renderer :isometric-staggered-tiled-map [opts]
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (load-tiled-map opts)
                                       ^double (unit-scale opts)))

(defmethod renderer :hexagonal-tiled-map [opts]
  (HexagonalTiledMapRenderer. ^TiledMap (load-tiled-map opts)
                              ^double (unit-scale opts)))

(defmethod renderer :stage [_]
  (Stage.))

; cameras

(defmulti camera identity :default :orthographic)

(defmethod camera :orthographic [_]
  (OrthographicCamera.))

(defmethod camera :perspective [_]
  (PerspectiveCamera.))

(defn resize-camera!
  [{:keys [^OrthographicCamera camera]} width height]
  (assert camera)
  (.setToOrtho camera false width height))

(defn move-camera!
  [{:keys [^Camera camera]} x y]
  (assert camera)
  (when x (set! (. (. camera position) x) x))
  (when y (set! (. (. camera position) y) y))
  (.update camera))