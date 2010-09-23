$(document).ready(function() {
    Listen.Attendant = function() {
        return {
            Application: function() {
                this.load = function() {
                    Listen.trace('Loading Attendant');
                };

                this.unload = function() {
                    Listen.trace('Unloading Attendant');
                };
            }
        }
    }();

    Listen.registerApp(new Listen.Application('attendant', 'attendant-application', 'menu-attendant', new Listen.Attendant.Application()));
});