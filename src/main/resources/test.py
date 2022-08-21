import requests


def reservationInfoPayload():
    payload = {
        "fullName": "bob",
        "email": "bob@gmail.com",
        "arrivalDate": "2022-08-20",
        "departureDate": "2022-08-23"
    }
    return payload


def requestCall(func):
    try:
        resp = func()
        print(resp.text)
        return resp.ok
    except requests.exceptions.RequestException as e:
        print(e)

    return False


def create(args):
    return requestCall(
        lambda: requests.post("http://localhost:8080/api/reservation/reservation", json=reservationInfoPayload(),
                              verify=False))


def available(args):
    start_date = '2022-08-18'
    end_date = "2022-08-30"
    return requestCall(
        lambda: requests.get("http://localhost:8080/api/reservation/reservations/" + start_date, verify= False))


def main(args):
    print("starting")
    create(args)
    available(args)


if __name__ == "__main__":
    from argparse import ArgumentParser

    parser = ArgumentParser(description='Send a request to service')
    parser.add_argument('-t', '--type', default="create")

    args = parser.parse_args()
    main(args)
