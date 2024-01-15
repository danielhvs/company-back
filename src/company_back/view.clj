(ns company-back.view
  (:require
   [clj-pdf.core :as pdf]
   [clojure.string :as str]
   [dali.io :as io]))

(def left-margin 85)
(def right-margin 55)
(def top-margin 46)
(def bottom-margin 25)
(def green [167 198 0])
(def orange [255 99 71])
(def white [255 255 255])
(def black [0 0 0])
(def gray [84 85 102])
(defn title-text [texto]
  [:heading.company {:style {:size 12 :align :right}} texto])

(defn title [texto]
  [:heading {:style {:style :bold :size 20 :color green :align :right}} texto])

(defn type->svg
  [{:keys [quantity name]
    :as _entity}]
  (get {"circle"
        [:circle {:cx "50", :cy "50", :r (str quantity) , :stroke "green", :stroke-width "4", :fill "yellow"}]
        "square"
        [:rect {:style "fill:blue;stroke-width:3;stroke:black"} [50 50] [50 50]]
        "triangle"
        [:polygon {:points "100,10 120,90  60,20", :style "fill:lime;stroke:purple;stroke-width:1"}]}
       name))

(defn svg*
  [entity]
  (io/render-svg-string
   [:dali/page
    (type->svg entity)]))

(defn svgs
  [data]
  (let [data (remove #(= "total" (:name %)) data)]
    (for [ent data]
      [[:svg {:translate (* 10 (:quantity ent))
              :rotate (:price ent)} (svg* ent)]])))

(defn pdf*
  [file-name client-name data]
  (into [[:spacer 5]
         [:spacer 13]
         (title "COMPANY SYSTEM\nPORTFOLIO")
         [:spacer 3]
         (title-text "Tech Proposal")
         (title-text file-name)
         [:spacer 11]
         [:heading {:style {:size 14 :color green :align :right}} "Client: " [:chunk {:color black} client-name]]]
        (svgs data)))

(def stylesheet
  {:company {:color gray}
   :right {:align :right}
   :fixme {:color orange :style :bold}
   :indice {:style :bold-italic :color gray}
   :center {:align :center}
   :meses {:set-border [:left] :color black :valign :middle :align :center :background-color green :size 7}
   :dados {:set-border [:right] :color black :valign :middle :align :right :background-color white :size 7}
   :heading {:border true :color black :valign :middle :align :center :background-color green :size 7}})

(defn pdf
  [data]
  (fn [output-stream]
    (try
      (pdf/pdf
       (let [client (str/upper-case "Client Name Stalone")
             file-name "arq"]
         (reduce into
                 [[{:footer {:text (str file-name (str/join (repeat 107 " ")))
                             :align :right
                             :color gray
                             :footer-separator "/"
                             :start-page 2}
                    :register-system-fonts? true
                    :pages true
                    :stylesheet stylesheet
                    :left-margin left-margin
                    :right-margin right-margin
                    :top-margin top-margin
                    :bottom-margin bottom-margin
                    :letterhead []
                    :font {:size 11 :encoding :unicode :ttf-name ".fonts/SegoePro-Regular.ttf"}}]
                  (pdf* file-name client data)]))
       output-stream)
      (catch Exception e (println e)))))

