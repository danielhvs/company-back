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
  (get {"Circle"
        [:circle {:cx "50", :cy "50", :r (str quantity) , :stroke "green", :stroke-width "4", :fill "yellow"}]
        "Losangle"
        [:polygon {:points "50,5 100,50 50,95 0,50", :style (str "fill:blue;stroke:black;stroke-width:" (min 5 quantity))}]
        "Square"
        [:rect {:style "fill:blue;stroke-width:3;stroke:black"} [50 50] [50 50]]
        "Triangle"
        [:polygon {:points "200,10 250,190 160,210", :style "fill:lime;stroke:purple;stroke-width:1"}]}
       name))

(defn svg
  [entities]
  (io/render-svg-string
    [:dali/page
     (type->svg (->> entities
                     (remove #(= "total" (:name %)))
                     shuffle
                     first))]))

(defn pdf*
  [file-name client-name data]
  [[:spacer 5]
   [:spacer 13]
   (title "COMPANY SYSTEM\nPORTFOLIO")
   [:spacer 3]
   (title-text "Tech Proposal")
   (title-text file-name)
   [:svg {} (svg data)]
   [:spacer 11]
   [:heading {:style {:size 14 :color green :align :right}} "Client: " [:chunk {:color black} client-name]]])

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
