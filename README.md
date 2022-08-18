# Brother Label Printer SDK Sample App
Welcome to the Brother Label Printer Android SDK Sample App.
I developed this sample app while working as a Technical Marketing Intern at Brother International
Corporation in order to demonstrate the available printing functionality.

The app is a sample Point-of-Sale System which can print sample labels that reflect situations in which
a commercial level Point-of-Sale System may aim to implement printing functionalities.

---
## Running the app

### Basic Functionality

The app can be run without a Brother Label printer, but you will only be able to take orders and fulfill them.
To run the app, all you need is the code provided in this project.

To start, you must add items for sale by going to "Log In As Vendor" -> "Manage Items" -> "Add Items".
Items are stored in the device's internal storage.
Once you have added items, you can place orders from the "Log In As Customer" menu.

Once orders are placed, they can be viewed in the "View Placed Orders" Activity of the Vendor Menu.
There, they can be printed (say a restaurant has a take-out bag prepared and they need to place
information about the order on it) or fulfilled.

Fulfilling an order is akin to having prepared all of the items in it. When an order is fulfilled,
it can be viewed in the "View Fulfilled Orders" Activity of the Vendor Menu. There, labels for individual
items can be printed. See below for instructions on configuring your printer with the proper template.

### Printing Functionality

In order to print, you must have a Brother QL Label Printer. To utilize all available functionality,
you must have a model which supports storage of templates on the system. The QL-800 series is recommended.

#### Connecting to a Printer

The app supports connection to a printer via Wi-Fi or Bluetooth. When connecting via Wi-Fi, there are
two possible methods to connect.

_The following is done on the "Printer Settings" page accessed from the Vendor Menu_

1.) You can search for available printers on the Wi-Fi network by selecting "Find Printer". The app
will then search for printers of the models that you have selected under "Model".

2.) You can manually enter the IP Address and Model. To do this, enter the IP under "Manual IP Address"
and the model under "Manual Model". Note that the Network Printer Status on the View Fulfilled Orders
and View Placed Orders will only show "Connected" if you search via the network.

If you choose to connect via Bluetooth, simply enter the Model and Mac Address of the printer.

#### Printing a Placed Order

Once you have connected to a printer, you can begin printing. From the "View Placed Orders" Page of the
Vendor Menu, you can print labels to go on a to-go bag with information about the order. You can utilize
the printer settings to customize your print job to your liking. Available options include AutoCut, which
can be toggled on or off, and Label Size.

#### Printing a Fulfilled Order

Provided with this project is the file SampleAppBarcode.lbx. This is the template file which is used to print
the items in fulfilled orders. In order to print such items, you must use P-touch Transfer Manager to
store this template on your printer. Then, set the Template Key to the value on your printer by going to the
Printer Settings Menu of Fulfilled Orders.

In order to print an individual item in a fulfilled order, you must use a 2.4" continuous label, as
that is the only label compatible with the template.
