package types

import "fmt"

type MarkFeatureCollection struct {
	Type     string        `json:"type"`
	Features []MarkFeature `json:"features"`
}

type MarkFeature struct {
	Type       string         `json:"type"`
	Properties map[string]any `json:"properties"`
	Geometry   MarkGeometry   `json:"geometry"`
}

type MarkGeometry struct {
	Coordinates []any  `json:"coordinates"`
	Type        string `json:"type"`
}

func (g MarkGeometry) String() string {
	switch g.Type {
	case "Point":
		l := len(g.Coordinates)
		switch l {
		case 2:
			return fmt.Sprintf("POINT (%v %v)", g.Coordinates[0], g.Coordinates[1])
		case 3:
			return fmt.Sprintf("POINTZ (%v %v %v)", g.Coordinates[0], g.Coordinates[1], g.Coordinates[2])
		case 4:
			return fmt.Sprintf("POINTZM (%v %v %v %v)", g.Coordinates[0], g.Coordinates[1], g.Coordinates[2], g.Coordinates[3])
		default:
			return "POINT EMPTY"
		}
	default:
		return "EMPTY"
	}
}
