package internal

type InventoryConfig struct {
	Application   string
	Service       string
	InventoryHost string
	WarehouseHost string

	Server string
	Token  string

	Cluster string
	Shard   string

	Source string

	// simulation
	SimFailCheckout   float32
	SimFailStyling    float32
	SimFailAvailable1 float32
	SimFailAvailable2 float32
	SimFailAvailable3 float32
	SimDelayChance    float32
	SimDelayMS        int // milliseconds
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

		Cluster: "us-west",
		Shard:   "primary",

		Source: "",

		SimFailCheckout:   0.1,
		SimFailAvailable1: 0.2,
		SimFailAvailable2: 0.1,
		SimDelayChance:    0.3333,
		SimDelayMS:        1000,
	}
}
