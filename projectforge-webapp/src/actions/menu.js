import { getServiceURL, handleHTTPErrors } from '../utilities/rest';

export const MENU_LOAD_BEGIN = 'MENU_LOAD_BEGIN';
export const MENU_LOAD_SUCCESS = 'MENU_LOAD_SUCCESS';
export const MENU_LOAD_FAILURE = 'MENU_LOAD_FAILURE';

export const loadBegin = () => ({
    type: MENU_LOAD_BEGIN,
});

export const loadSuccess = menu => ({
    type: MENU_LOAD_SUCCESS,
    payload: { menu },
});

export const loadFailure = error => ({
    type: MENU_LOAD_FAILURE,
    payload: { error },
});

export const loadMenu = () => (dispatch) => {
    dispatch(loadBegin());

    return fetch(
        getServiceURL('menu'),
        {
            method: 'GET',
            credentials: 'include',
        },
    )
        .then(handleHTTPErrors)
        .then(response => response.json())
        .then(({ mainMenu }) => dispatch(loadSuccess(mainMenu.menuItems)))
        .catch(error => dispatch(loadFailure(error)));
};
