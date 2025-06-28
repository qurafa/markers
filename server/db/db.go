package db

import (
	"context"
	"encoding/json"
	"fmt"
	"os"

	"github.com/jackc/pgx/v5"

	"example.com/marker/server/types"
)

var dbConn *pgx.Conn

func init() {
	var err error

	dbConn, err = pgx.Connect(context.Background(), os.Getenv("DATABASE_URL"))
	if err != nil {
		fmt.Fprintf(os.Stderr, "Unable to connect to database: %v\n", err)
		os.Exit(1)
	}

	if err := dbConn.Ping(context.Background()); err != nil {
		fmt.Fprintf(os.Stderr, "Unable to ping database: %v\n", err)
		os.Exit(1)
	}

	fmt.Println("Connected to DB...")
}

func AddMark(m *types.Mark) error {
	if m == nil {
		return fmt.Errorf("mark cannot be nil")
	}

	sql := `
	INSERT INTO markers (name, alias, notes, geom, img_url)
	VALUES ($1, $2, $3, ST_GeomFromText($4, 4326), $5)
	RETURNING id, created, updated;
	`

	// fmt.Print(m.Feature)

	//Note: not doing anything with the geom properties for now
	row := dbConn.QueryRow(
		context.Background(),
		sql,
		m.Name, m.Alias, m.Notes, m.Feature.Geometry.String(), m.ImgURL)

	if err := row.Scan(&m.ID, &m.Created, &m.Updated); err != nil {
		return fmt.Errorf("failed to insert mark: %v", err)
	}

	fmt.Printf("Inserted mark with ID: %d \n at %v", m.ID, m.Created)

	return nil
}

func GetMark(m *types.Mark) error {

	if m == nil {
		return fmt.Errorf("id can't be null")
	}

	sql := `SELECT name, alias, notes, ST_AsGeoJSON(geom) AS geojson, img_url, created, updated FROM markers WHERE id = $1`

	fmt.Println(m.ID)

	row := dbConn.QueryRow(
		context.Background(),
		sql,
		m.ID)

	var geoJSONData []byte

	if err := row.Scan(&m.Name, &m.Alias, &m.Notes, &geoJSONData, &m.ImgURL, &m.Created, &m.Updated); err != nil {
		return fmt.Errorf("failed to get mark: %v", err)
	}

	// fmt.Println(geoJSONData)

	// default types and properties for now, no implementation considering either for now
	m.Feature.Type = "Feature"
	m.Feature.Properties = map[string]any{}

	if gErr := json.Unmarshal(geoJSONData, &m.Feature.Geometry); gErr != nil {
		return fmt.Errorf("Unable to parse geom for id: %v : %v", m.ID, gErr)
	}

	fmt.Printf("Selected mark with ID: %d \n %v \n", m.ID, m)

	return nil
}

func GetAllMarks() (types.Marks, error) {
	var marks types.Marks

	sql := `SELECT id, name, alias, notes, ST_AsGeoJSON(geom) AS geojson, img_url, created, updated FROM markers`

	rows, dbErr := dbConn.Query(
		context.Background(),
		sql)

	if dbErr != nil {
		return types.Marks{}, fmt.Errorf("Unable to get all Marks: %v", dbErr)
	}

	defer rows.Close()

	for rows.Next() {
		var m types.Mark
		var geoJSONData []byte

		if err := rows.Scan(&m.ID, &m.Name, &m.Alias, &m.Notes, &geoJSONData, &m.ImgURL, &m.Created, &m.Updated); err != nil {
			return types.Marks{}, fmt.Errorf("failed to get mark: %v", err)
		}

		// default types and properties for now, no implementation considering either for now
		m.Feature.Type = "Feature"
		m.Feature.Properties = map[string]any{}

		if gErr := json.Unmarshal(geoJSONData, &m.Feature.Geometry); gErr != nil {
			return types.Marks{}, fmt.Errorf("Unable to parse geom for id: %v : %v", m.ID, gErr)
		}

		marks.Marks = append(marks.Marks, m)
	}

	return marks, nil
}

func CloseConnection() {
	dbConn.Close(context.Background())
}
