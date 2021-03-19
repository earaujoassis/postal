import Vue from "vue";
import Vuex, { StoreOptions } from "vuex";
import { RootState } from "./types";
import { emails } from "./modules/emails";
import { settings } from "./modules/settings";

Vue.use(Vuex);

const store: StoreOptions<RootState> = {
    state: {
        version: '1.0.0'
    },
    modules: {
        emails,
        settings
    }
};

export default new Vuex.Store<RootState>(store);
