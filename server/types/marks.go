package types

import (
	"time"
)

type Marks struct {
	Marks []Mark `json:"marks"`
}

type Mark struct {
	ID      string      `json:"id"`
	Name    string      `json:"name"`
	Alias   string      `json:"alias"`
	Notes   string      `json:"notes"`
	Feature MarkFeature `json:"feature"`
	ImgURL  string      `json:"img_url"`
	Created time.Time   `json:"created"`
	Updated time.Time   `json:"updated"`
}
