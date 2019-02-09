package internal

type InventoryConfig struct {
	Application   string
	Service       string
	InventoryHost string
	WarehouseHost string

	Server string
	Token  string

	// simulation
	SimFailCheckout  float32
	SimFailStyling   float32
	SimFailDelivery1 float32
	SimFailDelivery2 float32
	SimFailDelivery3 float32
	SimDelayChance   float32
	SimDelayMS       int // milliseconds
}

var GlobalConfig InventoryConfig

func InitGlobalConfig() {
	GlobalConfig = InventoryConfig{
		Application:   "",
		Service:       "",
		InventoryHost: "localhost:60001",
		WarehouseHost: "localhost:50060",

		Server: "",
		Token:  "",

		SimFailCheckout:  0.1,
		SimFailStyling:   0.2,
		SimFailDelivery1: 0.2,
		SimFailDelivery2: 0.1,
		SimFailDelivery3: 0.1,
		SimDelayChance:   0.3333,
		SimDelayMS:       1000,
	}
}
